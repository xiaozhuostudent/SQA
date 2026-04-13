package cn.edu.zjut.back.common;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    NOT_FOUND(404, "资源不存在"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限"),
    TOKEN_EXPIRED(402, "Token已过期"),
    USER_NOT_FOUND(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "密码错误"),
    USER_EXIST(1003, "用户已存在"),
    COURSE_NOT_FOUND(2001, "课程不存在"),
    ALREADY_ENROLLED(2002, "已选过该课程"),
    HOMEWORK_NOT_FOUND(3001, "作业不存在"),
    EXPERIMENT_NOT_FOUND(4001, "实验不存在"),
    PROBLEM_NOT_FOUND(4002, "题目不存在"),
    RESOURCE_NOT_FOUND(5001, "资源不存在"),
    SCHEDULE_CONFLICT(6001, "排班冲突");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
