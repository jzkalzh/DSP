package hk.bean;
import java.net.ConnectException;
import lombok.AllArgsConstructor;
import lombok.Data;
import hk.util.RandomNum;

@Data
@AllArgsConstructor
public class AppError {
    Integer error_code;
    String msg;

    public static  AppError build(){
        int errorCode = RandomNum.getRandInt(1001, 4001);
        String msg="模拟发生错误, 错误码:"+errorCode;
        return new AppError(errorCode,msg);
    }

    public static void main(String[] args) throws  Exception {
        throw  new ConnectException() ;
    }
}
