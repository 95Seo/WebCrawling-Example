package com.org.crawling.jinhakapply;

import com.org.crawling.jinhakapply.category.Category;
import com.org.crawling.jinhakapply.category.CategoryFactory;
import com.org.crawling.jinhakapply.category.type.CompetitionRatio;
import com.org.crawling.jinhakapply.category.type.RecruitmentCount;
import com.org.crawling.jinhakapply.category.type.SubDepartment;
import com.org.crawling.jinhakapply.support.CategoryReg;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.Invocation;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Component
public class UwayCrawlingTemplateV4 {

    // final 적용해도 될듯
    private static Map<String, String> categoryConvertMap = new HashMap<>();

    private static List<Category> categoryInfos = new LinkedList<>();

    private static DepartmentTemplate departmentTemplate;

    private static Queue<Category> categoryRemoveQueue = new LinkedList<>();

    private static Category deptCategory;

    private static int recuitSaveCnt = 0;

    private static int ratioSaveCnt = 0;

    // 한글 혹은 영어로 시작하나?

    public static void setCategoryConvertMap() {
        categoryConvertMap.put("캠퍼스","campus");
        categoryConvertMap.put("대학","university");
        categoryConvertMap.put("전형","admissionType");
        categoryConvertMap.put("모집단위","departmentName");
        categoryConvertMap.put("모집인원","recruitmentCount");
        categoryConvertMap.put("지원인원","applicantsCount");
        categoryConvertMap.put("경쟁률","competitionRatio");
        categoryConvertMap.put("성별", "sex");
        categoryConvertMap.put("종목  ", "event");
        // 뭔 ㅄ같은 ㅡㅡ
        // 학과 홍보
        // 학과 홈페이지
    }

    public static void main(String[] args) throws IOException {
        // 가톨릭꽃동네대학교 | 특수 케이스 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KMGE5OUpmJSY6JkpwZlRm";
//        String universityName = "가톨릭꽃동네대학교";

        // 감리교신학대학교 | 안할거임
//        String URL = "http://ratio.uwayapply.com/Sl5KJjBNSmYlJjomSnBmVGY=";
//        String universityName = "감리교신학대학교";

        // 강남대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KTThXclc4OUpmJSY6JkpwZlRm";
//        String universityName = "강남대학교";

        // 강원대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KV2FOclc4OUpmJSY6JkpwZlRm";
//        String universityName = "강원대학교";

        // 건국대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KOTo5V2E5SmYlJjomSnBmVGY=";
//        String universityName = "건국대학교";

        // 경기과학기술대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KMCZXJTlKZkNDYUxKcGZUZg==";
//        String universityName = "경기과학기술대학교";

        // 경기대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KJXJyV2FiOUpmJSY6JkpwZlRm";
//        String universityName = "경기대학교";

        // 경북대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KJjlKZiUmOiZKcGZUZg==";
//        String universityName = "경북대학교";

        // 경성대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KJjlKZiUmOiZKcGZUZg==";
//        String universityName = "경성대학교";

        // 경주대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KOi9yV2FgYnJKZiUmOiZKcGZUZg==";
//        String universityName = "경주대학교";

        // 경희대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KOnw5SmYlJjomSnBmVGY=";
//        String universityName = "경희대학교";

        // 계명대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KOk05SmYlJjomSnBmVGY=";
//        String universityName = "계명대학교";

        // 계원예술대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KV2FOYjg5SmZDQ2FMSnBmVGY=";
//        String universityName = "계원예술대학교";

        // 고려대학교(세종) | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KcldhL2AmOzhgfWE5SmYlJjomSnBmVGY=";
//        String universityName = "고려대학교(세종)";

        // 고려대학교(서울) | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KOGB9YTlKZiUmOiZKcGZUZg==";
//        String universityName = "고려대학교(서울)";

        // 고신대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KVyUmYTlKZiUmOiZKcGZUZg==";
//        String universityName = "고신대학교";

        // 공주교육대학교 | 안할거임
//        String URL = "http://ratio.uwayapply.com/Sl5KYDY6L3JXYTlKZiUmOiZKcGZUZg==";
//        String universityName = "공주교육대학교";

        // 광주교육대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KYDY6L3JXOE45SmYlJjomSnBmVGY=";
//        String universityName = "광주교육대학교";

        // 광주여자대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KOk45SmYlJjomSnBmVGY=";
//        String universityName = "광주여자대학교";

        // 국립금오공과대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KfGFNOjlKZiUmOiZKcGZUZg==";
//        String universityName = "국립금오공과대학교";

        // 국립목포대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KYWk5YU1KZiUmOiZKcGZUZg==";
//        String universityName = "국립목포대학교";

        // 극동대학교 | 특수케이스 (모집인원 (3)) | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KOlY5SmYlJjomSnBmVGY=";
//        String universityName = "극동대학교";

        // 금강대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KOnJySmYlJjomSnBmVGY=";
//        String universityName = "금강대학교";

        // 나사렛대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KOld9YTlKZiUmOiZKcGZUZg==";
//        String universityName = "나사렛대학교";

        // 남부대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KOkJNOFdKZiUmOiZKcGZUZg==";
//        String universityName = "남부대학교";

        // 대구예술대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KOjgwSmYlJjomSnBmVGY=";
//        String universityName = "대구예술대학교";

        // 대진대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KVyUvYDhWSmYlJjomSnBmVGY=";
//        String universityName = "대진대학교";

        // 동남보건대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KTThXcldhVkpmQ0NhTEpwZlRm";
//        String universityName = "동남보건대학교";

        // 동덕여자대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KOTpWcldhVkpmJSY6JkpwZlRm";
//        String universityName = "동덕여자대학교";

        // 동서울대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KOlZKZkNDYUxKcGZUZg";
//        String universityName = "동서울대학교";

        // 동신대학교 | 그래프 다름 | 실패
//        String URL = "http://ratio.uwayapply.com/Sl5KOlclfCZyV2FWSmYlJjomSnBmVGY=";
//        String universityName = "동신대학교";

        // 동아방송예술대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KOE0lVkpmQ0NhTEpwZlRm";
//        String universityName = "동아방송예술대학교";

        // 동의대학교
//        String URL = "http://ratio.uwayapply.com/Sl5KOmBWSmYlJjomSnBmVGY=";
//        String universityName = "동의대학교";

        // 루터대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KOjBDSmYlJjomSnBmVGY=";
//        String universityName = "루터대학교";

        // 명지전문대학교 | 성공
//        String URL = "https://ratio.uwayapply.com/Sl5KTC9NSmZDQ2FMSnBmVGY";
//        String universityName = "명지전문대학교";

        // 목포가톨릭대학교 | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KOkxNSmYlJjomSnBmVGY=";
//        String universityName = "목포가톨릭대학교";

        // 배재대학교 | 성공
        String URL = "http://ratio.uwayapply.com/Sl5KOkxpSmYlJjomSnBmVGY=";
        String universityName = "배재대학교";

        
        // ============================================ 성공 ============================================
        // 서울신학대학교
        // 모집인원, 경쟁률 칼럼 없음
//        String URL = "http://ratio.uwayapply.com/Sl5KOjAmSmYlJjomSnBmVGY=";
//        String universityName = "서울신학대학교";

        // 선문대학교
        // 학과 홍보라는 칼럼이 있음, 경쟁률 칼럼 없음
//        String URL = "http://ratio.uwayapply.com/Sl5KV2FhTVc6JkpmJSY6JkpwZlRm";
//        String universityName = "선문대학교";

        // 서울여자대학교
//        String URL = "http://ratio.uwayapply.com/Sl5KOk4mSmYlJjomSnBmVGY=";
//        String universityName = "서율여자대학교";


        // ============================================ 수정 필요 ============================================

        // 가톨릭관동대학교 | 특수케이스 성별/종목 (수정 필요) | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KcldhVlc4TjlKZiUmOiZKcGZUZg==";
//        String universityName = "가톨릭관동대학교";

        // 국민대학교 | 특수케이스 (모집인원 -), 경쟁률 -> 지원현황 (수정 필요) | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KVyVNOWFhOUpmJSY6JkpwZlRm";
//        String universityName = "국민대학교";

        // 대림대학교 | 특수케이스 경쟁률 -> 지원률 (수정 필요) | 성공
//        String URL = "http://ratio.uwayapply.com/Sl5KTSVDYDhWSmZDQ2FMSnBmVGY=";
//        String universityName = "대림대학교";
        
        
        // ============================================ 실패 ============================================
        // 서울과학대학교 <- 임마는 뭐고 추천인원이 모집인원으로 나온다
//        String URL = "http://ratio.uwayapply.com/Sl5KMDpXJkpmJSY6JkpwZlRm";
//        String universityName = "서울과학대학교";

        // 대구교육대학교 | 실패
//        String URL = "http://ratio.uwayapply.com/Sl5KYDpXVkpmJSY6JkpwZlRm";
//        String universityName = "대구교육대학교";

        

        departmentTemplate = new DepartmentTemplate(universityName);

        startCrawling(URL);

//        String input = " 단위 모집 ";
//        System.out.println("표현식 = "+input.matches(DEPT_REG));
    }

    public static void startCrawling(String URL) throws IOException {
        log.info("크롤링을 시작합니다.");

        Document document = Jsoup.connect(URL).get();

        // 경쟁률을 보여주는 테이블(div.DivType)을 Element로 복수개인 Elements를 만들어서 가져온다.
        Elements tables = document.select("form div.DivType");

        tableParsing(tables);

        log.info("크롤링이 끝났습니다.");
    }

    // 정원내 일반고(학생부) 경쟁률 현황 => 이거(입학 유형, AdmissionsType) 추출 후 다음 for 문
    private static void tableParsing(Elements tables) {
        // tableElements 안에 있는 table 들을 하나 씩 꺼내옴
        for (Element table : tables) {
            Elements tableCategory = table.select("thead tr th");

            String admissionType = table.select("div h3").text().replace("경쟁률 현황", "");
            if (!checkCategoryValid(tableCategory)) {
                log.info("{}", admissionType + "수집 X");
                continue;
            }

            // 일반고전형 경쟁률 현황(div h3)을 가져옴 (단일)
            log.info("{}", admissionType + "수집 O");
//            log.info("{}", e);

//            log.info("{}", tableCategory.text());

            departmentTemplate.setAdmissionType(admissionType);

            // 테이블의 row를 나누고 rows 로 만들어 가져옴
            Elements rows = table.getElementsByClass("trFieldValue");

            rowParsing(rows);

            resourceDeallocate();
        }
    }

    // 넘겨 받은 표를 학과별로 한 줄씩 나눈다.
    private static void rowParsing(Elements rows) {
        // rows의 각 row를 꺼내와서 하나 씩 반복
//        log.info("{}", rows.text());
//        log.info("{}", rows);
        for (Element row : rows) {
            // 한줄의 row를 column 으로 나눔
            Elements columns = row.getElementsByClass("txtFieldValue");

            columnPolicyExecute(columns);

            if ((recuitSaveCnt == 0) && (ratioSaveCnt == 0)) {
                log.info("-------------------------");
                departmentTemplate.printLog();
            }
        }
    }

    // DepartmentTemplate을 받아서 DepartmentTemplate을 다시 return하는 구조 수정해야 함
    public static void columnPolicyExecute(Elements columns) {
        int currentPoint = 0;
        int staticCategoryCnt = categoryInfos.size();
        int dynamicCategoryCnt = getSubDeptCount(columns);
        int subDeptCnt;

        // 사이즈가 크면 department 뒤에 sub 이어 붙이기
        if ((subDeptCnt = dynamicCategoryCnt - staticCategoryCnt) > 0) {
            log.info("===========================모집단위 나눠짐===========================");
            int deptSeq = deptCategory.getCategorySeq();
            for (int i = 1; i <= subDeptCnt; i++) {
                int subDeptSeq = deptSeq + i;
                categoryInfos.add(subDeptSeq, CategoryFactory.create("서브모집단위", subDeptSeq));
            }
        }

        // recuitSaveCnt != 0 && ratioSaveCnt != 0 이면 true
        // toLinked가 true면 문자열 연결하세요.
//        log.info("recuitSaveCnt = {}, ratioSaveCnt = {}", recuitSaveCnt, ratioSaveCnt);
        boolean toLinked = (recuitSaveCnt != 0) || (ratioSaveCnt != 0);

        for (Category info : categoryInfos) {
//            log.info("-------------------------");

            // rowCount 확인
            // rowCount가 0이면 column 값을 가져와서 CategoryInfo 객체에 셋팅
            if (info.rowCountIsZero()) {
                Element column = columns.get(currentPoint);
                info.setColumn(column);
                currentPoint++;
//                log.info("ratioRowCount1 = {}", info.getRowCount());
            }

            info.downRowCount();

            if (info.isTarget()) {
                info.setDepartmentProxyData(departmentTemplate, toLinked);
//                departmentTemplate.setParameter(info);
//                log.info("{} : {} ", info.getCategoryName(), info.getColumnData());
            }

            if (info instanceof RecruitmentCount) {
                recuitSaveCnt = info.getRowCount();
            }

            if (info instanceof CompetitionRatio) {
                if (!info.getColumnData().equals("-1")) {
//                    log.info("ratioRowCount2 = {}", info.getRowCount());
                    ratioSaveCnt = info.getRowCount();
                }
            }

            // instanceof -> 메서드 호출로 바꾸기
            if (info instanceof SubDepartment) {
                if (info.rowCountIsZero())
                    categoryRemoveQueue.add(info);
            }
        }

        // subDept 없애기
        for (Category removeTarget : categoryRemoveQueue) {
            categoryInfos.remove(removeTarget);
            removeTarget = null;
        }
    }

    private static int getSubDeptCount(Elements columns) {
        int count = 0;

        // html에서 가져온 columns.size + 현재 rowCount가 1이상인 categoryInfo의 갯수
        for (Category info : categoryInfos) {
            if (!info.rowCountIsZero())
                count += 1;
        }
        return columns.size() + count;
    }

    // 수정 포인트 제발 수정합시다.
    private static boolean checkCategoryValid(Elements tableCategory) {
        // 우리가 수집을 목표로 하는 2가지 타입의 카테고리
        List<String> categoryList = tableCategory.eachText();
        String print = "";
        for (String str : categoryList) {
            print += str + " ";
        }

        log.info("카테고리 = {}", print);

        if (
//                categoryList.stream().anyMatch(s -> s.matches(DEPT_REG) && s.matches(RECURITMENT_REG) && s.matches(APPLICANT_REG))
                categoryList.stream().anyMatch(s -> s.matches(CategoryReg.DEPARTMENT_REG.getReg())) &&
                categoryList.stream().anyMatch(s -> s.matches(CategoryReg.RECURITMENT_COUNT_REG.getReg())) &&
                categoryList.stream().anyMatch(s -> s.matches(CategoryReg.APPLICANTS_COUNT_REG.getReg()))
        ) {
            for (int i = 0; i < categoryList.size(); i++) {

                String category = categoryList.get(i);

                categoryInfos.add(CategoryFactory.create(category, i));

                // 모집인원 카테고리는 나뉘어지는 기준점이 된다.
                // 따로 빼서 바로 접근 할 수 있도록 하자.
                if (category.matches(CategoryReg.DEPARTMENT_REG.getReg()))
                    deptCategory = categoryInfos.get(i);
            }

            // 모집단위, 모집인원, 지원인원은 존재하는데,
            // 경쟁률만 없는 경우 경쟁률을 -1로 설정 해서 DB에 저장
            // 회원들 한테 보여줄 때 경쟁률 -1 이면 - 로 보여줌
            if (categoryList.stream().noneMatch(s -> s.matches(CategoryReg.COMPETITION_RATIO_REG.getReg()))) {
                log.info("경쟁률 생성!!");
                Category ratio = CategoryFactory.createRatio(Integer.MAX_VALUE);
                categoryInfos.add(ratio);
            }

            return true;
        }

        return false;
    }

    private static void resourceDeallocate() {
        categoryInfos.clear();
    }
}