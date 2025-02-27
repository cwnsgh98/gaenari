package com.gaenari.backend.domain.recordFeign.service;

import com.gaenari.backend.domain.client.program.dto.ProgramDetailDto;

import java.util.List;

public interface RecordFeignService {

    /**
     * 지정된 프로그램 ID에 대한 모든 기록을 조회합니다.
     *
     * @param programId 프로그램의 식별자입니다.
     * @return 해당 프로그램과 관련된 모든 기록의 사용 로그 목록을 반환합니다.
     */
    List<ProgramDetailDto.UsageLogDto> getRecordsByProgramId(Long programId);

    /**
     * 특정 회원의 운동 기록 ID에 대응하는 도전과제 ID 목록을 조회합니다.
     *
     * @param accountId 회원의 식별자입니다.
     * @param recordId 운동 기록의 식별자입니다.
     * @return 해당 운동 기록에 매칭되는 도전과제 ID 목록을 반환합니다.
     */
    List<Integer> getChallengeIdsByRecordId(String accountId, Long recordId);

    /**
     * 특정 회원의 운동 기록 ID에 대응하는 도전과제 보상 수령 여부를 완료로 변경합니다.
     *
     * @param accountId 회원의 식별자입니다.
     * @param recordId 운동 기록의 식별자입니다.
     */
    void updateRecordObtained(String accountId, Long recordId);

    /**
     * 특정 회원의 모든 운동 기록 도전과제 보상 수령 여부를 완료로 변경합니다.
     *
     * @param accountId 회원의 식별자입니다.
     */
    void updateAllRecordObtained(String accountId);
}
