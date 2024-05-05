package com.gaenari.backend.domain.member.service;

import com.gaenari.backend.domain.member.dto.MemberDto;
import com.gaenari.backend.domain.member.dto.requestDto.MemberUpdate;
import com.gaenari.backend.domain.member.dto.requestDto.MyPetDto;
import com.gaenari.backend.domain.member.dto.requestDto.SignupRequestDto;
import com.gaenari.backend.domain.member.dto.responseDto.SignupResponse;
import com.gaenari.backend.domain.member.entity.Member;
import com.gaenari.backend.domain.member.repository.MemberRepository;
import com.gaenari.backend.domain.mypet.entity.Dog;
import com.gaenari.backend.domain.mypet.entity.MyPet;
import com.gaenari.backend.domain.mypet.repository.DogRepository;
import com.gaenari.backend.domain.mypet.repository.MyPetRepository;
import com.gaenari.backend.global.exception.member.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.time.Duration;



@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{
    private final BCryptPasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final DogRepository dogRepository;
    private final MyPetRepository myPetRepository;

    @Override // 회원가입
    public SignupResponse createMember(SignupRequestDto requestDto) {
        // 이메일 중복 검증
        Member existMember = memberRepository.findByEmail(requestDto.getEmail());
        if (existMember != null) {
            throw new DuplicateEmailException();
        }

        // 엔티티에 회원등록
        Member member = Member.builder()
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .nickname(requestDto.getNickName())
                .birthday(requestDto.getBirth())
                .gender(requestDto.getGender())
                .height(requestDto.getHeight())
                .weight(requestDto.getWeight())
                .build();
        Member registMember = memberRepository.save(member);
        // 회원 반려견
        Dog dog = dogRepository.findById(requestDto.getMyPet().getId())
                .orElseThrow(DogNotFoundException::new);
        // 회원 반려견 등록
        MyPet myPet = MyPet.builder()
                .member(registMember)
                .dog(dog)
                .name(requestDto.getMyPet().getName())
                .isPartner(true)
                .build();
        MyPet registMyPet = myPetRepository.save(myPet);

        // 등록된 회원 불러오기
        MyPetDto myPetDto = MyPetDto.builder()
                .id(registMyPet.getDog().getId())
                .name(registMyPet.getName())
                .affection(registMyPet.getAffection())
                .tier(registMyPet.getTier())
                .changeTime(registMyPet.getChangeTime())
                .build();

        // response
        SignupResponse signupResponse = SignupResponse.builder()
                .memberId(registMember.getId())
                .nickName(registMember.getNickname())
                .myPet(myPetDto)
                .build();

        return signupResponse;
    }

    @Override // 이메일로 회원 찾기
    @Transactional(readOnly = true)
    public MemberDto getMemberDetailsByEmail(String memberEmail) {
        Member member = memberRepository.findByEmail(memberEmail);

        List<MyPet> myPetList = member.getMyPetList();
        MyPetDto myPetDto = null;
        for(MyPet myPet : myPetList){
            if(myPet.getIsPartner()){
                myPetDto = MyPetDto.builder()
                        .id(myPet.getDog().getId())
                        .name(myPet.getName())
                        .affection(myPet.getAffection())
                        .tier(myPet.getTier())
                        .changeTime(myPet.getChangeTime())
                        .build();
            }
        }

        MemberDto memberDto = MemberDto.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .birthday(member.getBirthday())
                .gender(member.getGender())
                .height(member.getHeight())
                .weight(member.getWeight())
                .coin(member.getCoin())
                .myPetDto(myPetDto)
                .build();

        return memberDto;
    }

    @Override // 회원 탈퇴
    public void deleteMember(String memberEmail) {
        // 회원 있는지 확인
        Member mem = memberRepository.findByEmail(memberEmail);

        memberRepository.delete(mem);
    }

    @Override // 보유코인 조회
    public int getCoin(String memberEmail) {
        Member mem = memberRepository.findByEmail(memberEmail);
        int coin = mem.getCoin();
        return coin;
    }

    @Override // 회원 닉네임 변경
    public void updateNick(String memberEmail, String nickName) {
        // 회원 조회
        Member member = memberRepository.findByEmail(memberEmail);

        if(member==null)
            throw new EmailNotFoundException();

        Member updateMember = Member.builder()
                .Id(member.getId())
                .email(member.getEmail())
                .password(member.getPassword())
                .nickname(nickName) // 변경
                .birthday(member.getBirthday())
                .gender(member.getGender())
                .height(member.getHeight())
                .weight(member.getWeight())
                .coin(member.getCoin())
                .device(member.getDevice())
                .myPetList(member.getMyPetList())
                .build();
        memberRepository.save(updateMember);

    }

    @Override // 회원 비밀번호 변경
    public void updatePwd(String memberEmail, String newPassword) {
        // 회원 조회
        Member member = memberRepository.findByEmail(memberEmail);

        if(member==null)
            throw new EmailNotFoundException();

        Member updateMember = Member.builder()
                .Id(member.getId())
                .email(member.getEmail())
                .password(passwordEncoder.encode(newPassword)) // 변경
                .nickname(member.getNickname())
                .birthday(member.getBirthday())
                .gender(member.getGender())
                .height(member.getHeight())
                .weight(member.getWeight())
                .coin(member.getCoin())
                .device(member.getDevice())
                .myPetList(member.getMyPetList())
                .build();
        memberRepository.save(updateMember);
    }

    @Override // 회원 정보 변경
    public void updateInfo(String memberEmail, MemberUpdate memberUpdate) {
        // 회원 조회
        Member member = memberRepository.findByEmail(memberEmail);

        if(member==null)
            throw new EmailNotFoundException();

        Member updateMember = Member.builder()
                .Id(member.getId())
                .email(member.getEmail())
                .password(member.getPassword())
                .nickname(member.getNickname())
                .birthday(member.getBirthday())
                .gender(member.getGender())
                .height(memberUpdate.getHeight()) // 변경
                .weight(memberUpdate.getWeight()) // 변경
                .coin(member.getCoin())
                .device(member.getDevice())
                .myPetList(member.getMyPetList())
                .build();
        memberRepository.save(updateMember);
    }

    @Override // 닉네임 중복확인
    public Boolean duplNickNameCheck(String nickName) {
        Member member = memberRepository.findByNickname(nickName);
        if(member == null){
            return false;
        }else{
            return true;
        }
    }

    @Override // 이메일 중복확인
    public Boolean duplEmailCheck(String email) {
        Member member = memberRepository.findByEmail(email);
        if(member == null){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(username);

        if(member==null)
            throw new EmailNotFoundException();

        return new User(member.getEmail(), member.getPassword(),
                true, true, true, true,
                new ArrayList<>());
    }
    @Override // 워치 인증번호생성
    public String issuedAuthCode(String memberEmail) {
        Member member = memberRepository.findByEmail(memberEmail);

        // 인증번호 생성 로직
        String authCode = generateVerificationCode();

        // 생성된 인증번호와 현재 시간을 멤버 엔티티에 저장
        Member saveMember = Member.builder()
                .Id(member.getId())
                .email(member.getEmail())
                .password(member.getPassword())
                .nickname(member.getNickname())
                .birthday(member.getBirthday())
                .gender(member.getGender())
                .height(member.getHeight())
                .weight(member.getWeight())
                .coin(member.getCoin())
                .device(authCode)                 // 생성된 인증번호 저장
                .deviceTime(LocalDateTime.now())  // 인증번호 생성시간 저장
                .myPetList(member.getMyPetList())
                .build();

        // 변경된 내용을 데이터베이스에 저장
        memberRepository.save(saveMember);

        return authCode;
    }
    private String generateVerificationCode() {
        // 인증번호 생성 로직 구현
        // 예시로 랜덤한 숫자 4자리로 생성하는 코드 작성
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }

    @Override
    public MemberDto checkAuthCode(String authCode) {
        // 1. 인증번호랑 확인하는 member를 가지고 오기, 없으면 예외처리
        Member member = memberRepository.findByDevice(authCode);
        if(member == null){
            throw new MemberNotFoundException();
        }
        // 2. member에서 인증번호 발급시간에서 3분이내인지 확인, 예외처리
        LocalDateTime deviceTime = member.getDeviceTime();
        LocalDateTime currentTime = LocalDateTime.now();
        Duration duration = Duration.between(deviceTime, currentTime);
        // 차이가 3분 이내인지 확인
        Boolean timeCheck = duration.getSeconds() > 180; // 3분은 180초
        if(timeCheck){
            throw new OverTimeAuthCodeException();
        }
        // 3. MemberDto 변환
        // 4. 파트너 반려견 찾기
        List<MyPet> myPetList = member.getMyPetList();
        MyPetDto myPetDto = null;
        for(MyPet myPet : myPetList){
            if(myPet.getIsPartner()){
                myPetDto = MyPetDto.builder()
                        .id(myPet.getDog().getId())
                        .name(myPet.getName())
                        .affection(myPet.getAffection())
                        .tier(myPet.getTier())
                        .changeTime(myPet.getChangeTime())
                        .build();
            }
        }
        MemberDto memberDto = MemberDto.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .birthday(member.getBirthday())
                .gender(member.getGender())
                .height(member.getHeight())
                .weight(member.getWeight())
                .coin(member.getCoin())
                .myPetDto(myPetDto)
                .build();

        return memberDto;
    }





}
