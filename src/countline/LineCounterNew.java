package countline;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class LineCounterNew {

    // 소스 디렉토리의 경로를 저장할 멤버 변수
    private final String srcDirectory;
    // 제외할 폴더 리스트
    private final List<String> excludeFolders;
    // 제외할 파일 확장자 목록
    private final List<String> excludeFileExtensions;

    public LineCounterNew(Properties properties) {
//        // Properties 객체로부터 설정을 읽어옴
//        String rawSrcDirectory = properties.getProperty("srcDirectory");
//        // 경로 내의 모든 단일 역슬래시(\)를 두 개의 역슬래시(\\)로 변경
//        this.srcDirectory = rawSrcDirectory.replace("\\", "\\\\");
        // 생성자에서 Properties 객체로부터 설정을 읽어옴
        this.srcDirectory = properties.getProperty("srcDirectory", "default");
        this.excludeFolders = Arrays.asList(
                properties.getProperty("excludeFolders", "").split("\\|"));
        this.excludeFileExtensions = Arrays.asList(
                properties.getProperty("excludeFileExtensions", "").split("\\|"));
    }

    public static void main(String[] args) {
        // Properties 객체 생성: 설정 정보를 저장하기 위함
        Properties properties = new Properties();

        // LineCounterNew 클래스의 클래스 로더를 통해
        // 'config/config.properties' 경로의 리소스 스트림을 얻음
        try (InputStream inputStream = LineCounterNew.class.getClassLoader()
                .getResourceAsStream("resources/config/config.properties"))
        {
            // 설정 파일이 없을 경우 예외 처리
            if (inputStream == null) {
                throw new IOException("Configuration file not found");
            }
            // Properties 객체에 설정 파일로부터 읽어온 정보 로드
            properties.load(inputStream);

            // LineCounterNew 인스턴스 생성, 생성자에 Properties 객체 전달
            LineCounterNew counter = new LineCounterNew(properties);
            // srcDirectory 문자열에서 마지막 '\\' 문자 뒤의 부분을 추출하여 dir 변수에 저장
            // 이는 srcDirectory 경로의 마지막 폴더 이름을 얻기 위함임
//            String dir = counter.srcDirectory
//                    .substring(counter.srcDirectory.lastIndexOf(File.separator) + 1);
            // countLinesByFileType 메서드를 호출하여 파일 종류별 라인 수를 계산, 결과는 Map 객체에 저장
            Map<String, Long> linesByFileType = counter.countLinesByFileType();
            // 계산된 라인 수를 파일 종류별로 출력
            linesByFileType.forEach((type, lines)
                    -> System.out.println(type + " files total lines : " + lines));
            // 전체 라인 수 계산: Map 객체의 values 컬렉션을 스트림으로 변환한 뒤,
            // Long::longValue 를 통해 long 값으로 매핑하고, 최종적으로 sum 메서드로 모든 값을 합산
            long totalLines = linesByFileType.values()
                    .stream().mapToLong(Long::longValue).sum();
            System.out.println("Total lines in '" + counter.srcDirectory + "' directory : " + totalLines);
        } catch (IOException e) {
            System.err.println("Error reading files: " + e.getMessage());
        }
    }

    // 파일 종류별 라인 수 계산 메서드
    public Map<String, Long> countLinesByFileType() throws IOException, IllegalArgumentException {
        Path path = Paths.get(srcDirectory); // String 경로를 Path 객체로 변환
        if (!Files.exists(path) || !Files.isDirectory(path)) { // 경로의 존재 여부 및 디렉토리 여부 확인
            // 예외 발생
            throw new IllegalArgumentException("경로가 존재하지 않거나 디렉토리가 아닙니다 -> "
                    + srcDirectory);
        }

        // 파일 종류별 라인 수를 저장할 맵 생성
        Map<String, Long> lineCountByType = new HashMap<>();

        // 모든 파일을 리스트로 생성
        List<Path> files = Files.walk(path)     // 주어진 경로에서 시작하여 모든 파일 탐색
                .filter(Files::isRegularFile)   // 파일인지 확인
                .filter(this::isValidPath) // isValidPath 메서드를 참조하여 조건을 만족하는 파일만 필터링
                .collect(Collectors.toList());  // 결과를 리스트로 수집

        for (Path file : files) {   // 모든 파일에 대해 반복
            String fileName = file.getFileName().toString();    // 파일 이름 추출
            // 마지막 '.' 이후의 문자열을 추출해 fileType에 저장 -> 파일 확장자 추출
            // 확장자가 없는 경우 "Unknown" 으로 출력
            String fileType = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "Unknown";

            // 파일을 읽어 각 파일의 라인 수를 계산 (try-with-resources 구문)
            try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile()))) {
                long lines = reader.lines().count();    // 현재 파일의 라인 수를 계산
                lineCountByType.merge(fileType, lines, Long::sum);  // 같은 확장자를 가진 파일들의 라인 수를 합산
            }
        }

        return lineCountByType; // 계산된 파일 종류별 라인 수를 담은 맵을 반환
    }

    private boolean isValidPath(Path path) {
        String pathStr = path.toString();
        // 제외할 폴더를 포함하고 있는지 확인
        for (String folder : excludeFolders) {
            if (pathStr.contains(folder)) {
                return false; // 하나라도 포함되어 있으면 false 반환
            }
        }
        // 파일 확장자 체크 추가
        for (String extension : excludeFileExtensions) {
            if (pathStr.endsWith(extension)) {
                return false;
            }
        }
        return true; // 모든 제외 폴더를 포함하지 않으면 true 반환
    }
}