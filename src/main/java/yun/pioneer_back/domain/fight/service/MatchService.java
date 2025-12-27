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
            isAcquired = lock.tryLock(0, 10, TimeUnit.SECONDS);
            if(!isAcquired) return;

            // 큐에서 2명 pop
            Set<Object> userIdSet = redisUtil.zsetGet(REDIS_KEY_MATCH_WAITING_QUEUE, false, 0, 1);
            List<Long> userIdList = userIdSet.stream()
                    .map(userId -> Long.valueOf(userId.toString()))
                    .toList();

            // 2명 미만이라면 종료
            if(userIdList.size() < 2) {
                return;
            }

            // 유저 ID 조회
            Long userAId = userIdList.get(0);
            Long userBId = userIdList.get(1);

            // 매칭 상태 조회
            Object userAMatchStateRaw = redisUtil.valueGet(fightMatchUserStateKey(userAId));
            Object userBMatchStateRaw = redisUtil.valueGet(fightMatchUserStateKey(userBId));

            MatchState userAmatchState = (userAMatchStateRaw == null) ?
                    MatchState.IDLE :
                    MatchState.valueOf(userAMatchStateRaw.toString());
            MatchState userBmatchState = (userBMatchStateRaw == null) ?
                    MatchState.IDLE :
                    MatchState.valueOf(userBMatchStateRaw.toString());

            // 둘 중 한 명이라도 "매칭 대기" 상태가 아니라면 종료
            if(!MatchState.WAITING.equals(userAmatchState)) {
                redisUtil.zsetDelete(REDIS_KEY_MATCH_WAITING_QUEUE, userAId);
                return;
            }
            if(!MatchState.WAITING.equals(userBmatchState)) {
                redisUtil.zsetDelete(REDIS_KEY_MATCH_WAITING_QUEUE, userBId);
                return;
            }

            // 유저 조회
            User userA = userRepository.findById(userAId)
                    .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, userAId));
            User userB = userRepository.findById(userBId)
                    .orElseThrow(() -> new CustomException(CustomExceptionCode.USER_NOT_FOUND, userBId));

            // 결투 세션 생성 및 저장
            FightSession fightSession = FightSession.create(userA, userB);
            fightSessionRepository.save(fightSession);

            // 방 UUID 조회
            String roomId = fightSession.getRoomId();

            // 유저 매칭 상태를 "매칭 완료"로 변경
            redisUtil.valueAdd(fightMatchUserStateKey(userAId), MatchState.MATCHED);
            redisUtil.valueAdd(fightMatchUserStateKey(userBId), MatchState.MATCHED);

            // 유저가 매칭된 방 UUID 갱신
            redisUtil.valueAdd(fightMatchRoomIdKey(userAId), roomId);
            redisUtil.valueAdd(fightMatchRoomIdKey(userBId), roomId);

            // 매칭 완료 WebSocket 메시지 전송
            messagingTemplate.convertAndSendToUser(
                    userAId.toString(),
                    "/sub/match",
                    FightWebSocketMessage.create(
                            MatchFoundDetails.builder()
                                    .roomId(roomId)
                                    .myUserId(userAId)
                                    .myNickname(userA.getNickname())
                                    .opponentUserId(userBId)
                                    .opponentNickname(userB.getNickname())
                                    .build()
                    )
            );
            messagingTemplate.convertAndSendToUser(
                    userBId.toString(),
                    "/sub/match",
                    FightWebSocketMessage.create(
                            MatchFoundDetails.builder()
                                    .roomId(roomId)
                                    .myUserId(userBId)
                                    .myNickname(userB.getNickname())
                                    .opponentUserId(userAId)
                                    .opponentNickname(userA.getNickname())
                                    .build()
                    )
            );

            // 큐에서 해당 유저들 데이터 제거
            redisUtil.zsetDelete(REDIS_KEY_MATCH_WAITING_QUEUE, userAId);
            redisUtil.zsetDelete(REDIS_KEY_MATCH_WAITING_QUEUE, userBId);

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
