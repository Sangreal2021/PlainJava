package parsing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

// 소스폴더에서 매 num 번째 파일들을 대상폴더로 이동

public class RefFileMover {

    // 경로 정보를 클래스의 멤버 변수로 선언
    // Paths.get()은 주어진 문자열 경로를 Path 객체로 변환
    private final Path sourceDir = Paths.get("D:\\vagel\\SITE\\유지보수\\790.한국전력\\전사작업\\녹취분류(150시간)\\장정수\\나머지녹취(40h)");
    private final Path destinationDir = Paths.get("D:\\vagel\\SITE\\유지보수\\790.한국전력\\전사작업\\녹취분류(150시간)\\spare\\others_01");

    public static void main(String[] args) {
        RefFileMover fileMover = new RefFileMover();
        fileMover.moveEveryNthFile(3); // 여기서 3은 매 3번째 파일을 의미(파일을 이동시키는 간격을 정의)
    }

    // num 번째 파일을 이동하는 기능을 별도의 메소드로 분리
    public void moveEveryNthFile(int num) {
        // Files.list 메서드를 사용하여 소스 디렉터리의 모든 파일을 나타내는 Stream<Path> 객체를 생성
        // try-with-resources 문은 Stream 이 사용 후 자동으로 닫히도록 보장
        try (Stream<Path> stream = Files.list(sourceDir)) {
            int[] count = {0};  // 파일 카운터를 증가
            stream.forEach(file -> {
                count[0]++;
                // if 문을 사용하여 카운터 값이 num의 배수일 때마다 파일을 이동시킴
                // count[0] % num은 카운터 값을 num으로 나눈 나머지를 계산하고, == 1은 매 num번째 파일을 선택
                if (count[0] % num == 1) {
                /*  1. file:
                		이것은 이동시키고자 하는 소스 파일의 Path 객체입니다.
                		Files.list 메서드로부터 얻은 파일 목록에서 현재 처리 중인 파일을 나타냅니다.

                	2. destinationDir.resolve(file.getFileName()):
                		destinationDir은 대상 디렉터리의 Path 객체입니다.
                		resolve 메서드는 주어진 Path 객체(여기서는 destinationDir)에
                		다른 Path 객체(여기서는 file.getFileName())를 결합하여 새로운 Path 객체를 생성합니다.
                		이는 기본적으로 대상 디렉터리 경로에 파일 이름을 추가하는 것과 같습니다.
                		file.getFileName()는 현재 처리 중인 파일의 이름만을 반환합니다.
                		따라서, 이 코드는 소스 파일을 대상 디렉터리로 이동시키되, 파일 이름은 유지합니다.

                	3. StandardCopyOption.REPLACE_EXISTING:
                		이것은 Files.move 메서드에 전달되는 옵션 중 하나로, 만약 대상 위치에
                		같은 이름의 파일이 이미 존재한다면, 그 파일을 대체하라는 지시입니다.
                		REPLACE_EXISTING 옵션을 사용하지 않으면, 동일한 이름의 파일이 대상 디렉터리에
                		이미 존재하는 경우 FileAlreadyExistsException 예외가 발생합니다.    */
                    try {
                        Files.move(file, destinationDir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Moved: " + file.getFileName());
                    } catch (IOException e) {
                        e.getMessage();
                    }
                }
            });
        } catch (IOException e) {
            e.getMessage();
        }
    }
}
