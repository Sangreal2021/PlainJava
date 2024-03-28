package validator;

import java.util.Scanner;

public class InputValidator {

    private Scanner sc;

    public static void main(String[] args){
        InputValidator validator = new InputValidator();
        String validInput = validator.getValidInput();

        System.out.println("정상 처리된 입력: " + validInput);
        validator.closeScanner();
    }

    public InputValidator(){
        this.sc = new Scanner(System.in);
    }

    public String getValidInput(){
        String input = "";
        String result = "";
        boolean check = false;

        while(!check){
            // while(true)이므로 무한반복문
            System.out.print("은행코드 입력 (3자리 숫자) >>> ");
            input = sc.nextLine();

            // while(false)가 되면 반복문 탈출
            if(isValidInput(input)){
                check = true;
                result = input;
            }else{
                System.out.println("오류: 입력은 한글, 영문, 특수문자를 " +
                        "포함하지 않는 정확히 3자리의 숫자여야 합니다.");
            }
        }
        return result;
    }

    private boolean isValidInput(String input){
        return input.matches("[0-9]{3}");
    }

    public void closeScanner(){
        this.sc.close();
    }
}
