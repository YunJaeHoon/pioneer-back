package yun.pioneer_back.domain.fight.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yun.pioneer_back.common.entity.rdbms.FightSession;
import yun.pioneer_back.common.entity.rdbms.User;
import yun.pioneer_back.common.exception.CustomException;
import yun.pioneer_back.common.exception.CustomExceptionCode;
import yun.pioneer_back.common.repository.rdbms.FightSessionRepository;
import yun.pioneer_back.common.repository.rdbms.UserRepository;
import yun.pioneer_back.common.util.RedisUtil;
import yun.pioneer_back.domain.fight.dto.message.FightWebSocketMessage;
import yun.pioneer_back.domain.fight.dto.message.details.MatchFoundDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.lang.System.getenv;

@Service
@RequiredArgsConstructor
public class MatchService
{
    private final RedissonClient redisson;
    private final SimpMessagingTemplate messagingTemplate;

    private final UserRepository userRepository;
    private final FightSessionRepository fightSessionRepository;

    private final RedisUtil redisUtil;

    // 유저 매칭 상태를 저장할 redis key의 접두사
    private final String REDIS_PREFIX_FIGHT_MATCH_USER_STATE = getenv("REDIS_PREFIX_FIGHT_MATCH_USER_STATE");

    // 유저가 매칭된 방 UUID를 저장할 redis key의 접두사
    private final String REDIS_PREFIX_FIGHT_MATCH_ROOM_ID = getenv("REDIS_PREFIX_FIGHT_MATCH_ROOM_ID");

    // 매칭 대기열 redis key & lock
    private final String REDIS_KEY_MATCH_WAITING_QUEUE = getenv("REDIS_KEY_MATCH_WAITING_QUEUE");
    private final String REDIS_LOCK_MATCH_WAITING_QUEUE =  getenv("REDIS_LOCK_MATCH_WAITING_QUEUE");

    /// ============ service ============

    // 랜덤 매칭 시작
    @Transactional
    public void request(User user)
    {
        // 매칭 상태 조회
        String matchStateKey = fightMatchUserStateKey(user.getId());
        Object matchStateRaw = redisUtil.valueGet(matchStateKey);
        MatchState matchState = (matchStateRaw == null) ?
                MatchState.IDLE :
                MatchState.valueOf(matchStateRaw.toString());

        // 이미 매칭 대기 혹은 매칭 완료라면 중복 대기 방지
        if(matchState.equals(MatchState.WAITING) || matchState.equals(MatchState.MATCHED)) {
            return;
        }

        // 유저 매칭 상태를 "매칭 대기"로 변경
        redisUtil.valueAdd(matchStateKey, MatchState.WAITING);

        // 유저를 매칭 대기열(ZSet)에 추가 (score : 현재 시간)
        redisUtil.zsetAdd(REDIS_KEY_MATCH_WAITING_QUEUE, user.getId(), System.currentTimeMillis());

        // 랜덤 매칭 시도
        tryMatch();
    }

    // 랜덤 매칭 시도 (큐에서 2명을 뽑아 매치 성사)
    @Transactional
    public void tryMatch()
    {
        // 분산 락
        RLock lock = redisson.getLock(REDIS_LOCK_MATCH_WAITING_QUEUE);

        // 분산 락 획득 여부
        boolean isAcquired = false;

        try {
            // 분산 락 획득 시도
            isAcquired = lock.tryLock(0, 30, TimeUnit.SECONDS);
            if(!isAcquired) return;

            // 선택된 2명이 들어갈 유저 및 score 리스트
            List<User> selectedUserList = new ArrayList<>();
            List<Double> selectedUserScoreList = new ArrayList<>();

            // 2명이 선택될 때까지 반복
            while(selectedUserList.size() != 2)
            {
                // 큐에서 1명 꺼내기
                Set<Object> userIdSet = redisUtil.zsetGet(REDIS_KEY_MATCH_WAITING_QUEUE, false, 0, 0);

                // 큐에 아무도 없다면 종료
                if(userIdSet.isEmpty()) {
                    break;
                }

                // 유저 ID 조회
                Long userId = Long.valueOf(userIdSet.iterator().next().toString());

                // score 조회
                // 조회가 안된다면 큐에서 제거하고 continue
                Double scoreObj = redisUtil.zsetGetScore(REDIS_KEY_MATCH_WAITING_QUEUE, userId);
                if (scoreObj == null) {
                    redisUtil.zsetDelete(REDIS_KEY_MATCH_WAITING_QUEUE, userId);
                    continue;
                }
                double score = scoreObj;

                // 큐에서 제거
                redisUtil.zsetDelete(REDIS_KEY_MATCH_WAITING_QUEUE, userId);

                // 유저 매칭 상태 조회
                Object userMatchStateRaw = redisUtil.valueGet(fightMatchUserStateKey(userId));
                MatchState userMatchState = (userMatchStateRaw == null) ?
                        MatchState.IDLE :
                        MatchState.valueOf(userMatchStateRaw.toString());

                // "매칭 대기" 상태가 아니라면 continue
                if(!MatchState.WAITING.equals(userMatchState)) {
                    continue;
                }

                // 유저 조회 및 선택된 유저 리스트에 포함
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, userId));
                selectedUserList.add(user);

                // 유저 score 리스트에 해당 유저의 score 포함
                selectedUserScoreList.add(score);
            }

            // 선택된 유저가 2명 미만이라면 다시 큐에 넣기
            if(selectedUserList.size() < 2)
            {
                for(int i = 0; i < selectedUserList.size(); i++)
                {
                    redisUtil.zsetAdd(
                            REDIS_KEY_MATCH_WAITING_QUEUE,
                            selectedUserList.get(i).getId(),
                            selectedUserScoreList.get(i)
                    );
                }
            }
            else
            {
                User userA = selectedUserList.get(0);
                User userB = selectedUserList.get(1);

                // 결투 세션 생성 및 저장
                FightSession fightSession = FightSession.create(userA, userB);
                fightSessionRepository.save(fightSession);

                // 방 UUID 조회
                String roomId = fightSession.getRoomId();

                // 유저 매칭 상태를 "매칭 완료"로 변경
                redisUtil.valueAdd(fightMatchUserStateKey(userA.getId()), MatchState.MATCHED);
                redisUtil.valueAdd(fightMatchUserStateKey(userB.getId()), MatchState.MATCHED);

                // 유저가 매칭된 방 UUID 갱신
                redisUtil.valueAdd(fightMatchRoomIdKey(userA.getId()), roomId);
                redisUtil.valueAdd(fightMatchRoomIdKey(userB.getId()), roomId);

                // 매칭 완료 WebSocket 메시지 전송
                messagingTemplate.convertAndSendToUser(
                        userA.getId().toString(),
                        "/sub/match",
                        FightWebSocketMessage.create(
                                MatchFoundDetails.builder()
                                        .roomId(roomId)
                                        .myUserId(userA.getId())
                                        .myNickname(userA.getNickname())
                                        .opponentUserId(userB.getId())
                                        .opponentNickname(userB.getNickname())
                                        .build()
                        )
                );
                messagingTemplate.convertAndSendToUser(
                        userB.getId().toString(),
                        "/sub/match",
                        FightWebSocketMessage.create(
                                MatchFoundDetails.builder()
                                        .roomId(roomId)
                                        .myUserId(userB.getId())
                                        .myNickname(userB.getNickname())
                                        .opponentUserId(userA.getId())
                                        .opponentNickname(userA.getNickname())
                                        .build()
                        )
                );
            }

        } catch(Exception e) {
            throw new CustomException(CustomExceptionCode.REDISSON_OPERATION_ERROR, e.getMessage());
        } finally {
            // 분산 락 해제
            if (isAcquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /// ============ enum ============

    // 유저 매칭 상태
    @AllArgsConstructor
    public enum MatchState
    {
        IDLE("매칭 전"),
        WAITING("매칭 대기"),
        MATCHED("매칭 완료");

        private final String description;
    }

    /// ============ util ============

    // 유저 매칭 상태를 저장할 redis key
    private String fightMatchUserStateKey(Long userId) {
        return REDIS_PREFIX_FIGHT_MATCH_USER_STATE + userId;
    }

    // 유저가 매칭된 방 UUID를 저장할 redis key
    private String fightMatchRoomIdKey(Long userId) {
        return REDIS_PREFIX_FIGHT_MATCH_ROOM_ID + userId;
    }
}
