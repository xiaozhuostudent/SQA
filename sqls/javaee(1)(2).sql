/*
 Navicat Premium Dump SQL

 Source Server         : Javaee
 Source Server Type    : MySQL
 Source Server Version : 80405 (8.4.5)
 Source Host           : 120.26.212.210:3306
 Source Schema         : javaee

 Target Server Type    : MySQL
 Target Server Version : 80405 (8.4.5)
 File Encoding         : 65001

 Date: 23/12/2025 20:45:34
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for live_streams
-- ----------------------------
DROP TABLE IF EXISTS `live_streams`;
CREATE TABLE `live_streams`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '直播标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '直播描述',
  `teacher_id` bigint NOT NULL COMMENT '教师ID',
  `teacher_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '教师姓名',
  `course_id` bigint NULL DEFAULT NULL COMMENT '关联课程ID',
  `course_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '课程名称',
  `status` enum('scheduled','live','ended') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'scheduled' COMMENT '状态',
  `stream_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推流密钥',
  `stream_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推流地址',
  `play_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '播放地址',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片',
  `scheduled_time` datetime NULL DEFAULT NULL COMMENT '预定开始时间',
  `start_time` datetime NULL DEFAULT NULL COMMENT '实际开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `viewer_count` int NULL DEFAULT 0 COMMENT '当前观看人数',
  `total_views` int NULL DEFAULT 0 COMMENT '累计观看次数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_scheduled_time`(`scheduled_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '直播表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of live_streams
-- ----------------------------
INSERT INTO `live_streams` VALUES (11, '55', '55', 3, '张教授', 2, 'Web开发技术', 'ended', 'e02b01f1b1cc4edeb53d9622cbcb2bfa', 'rtmp://localhost:8000/live/e02b01f1b1cc4edeb53d9622cbcb2bfa', 'http://localhost:8090/live/e02b01f1b1cc4edeb53d9622cbcb2bfa.m3u8', NULL, '2025-12-04 01:47:12', '2025-12-04 01:47:24', '2025-12-05 01:11:58', 1, 1, '2025-12-04 01:47:20', '2025-12-05 01:11:58');
INSERT INTO `live_streams` VALUES (12, '66', '66', 3, '张教授', 3, '操作系统', 'ended', '31c2d17f2396432abe16ef6b47db8057', 'rtmp://localhost:8000/live/31c2d17f2396432abe16ef6b47db8057', 'http://localhost:8090/live/31c2d17f2396432abe16ef6b47db8057.m3u8', NULL, '2025-12-05 01:13:00', '2025-12-05 01:12:14', '2025-12-05 01:12:20', 0, 0, '2025-12-05 01:12:10', '2025-12-05 01:12:20');
INSERT INTO `live_streams` VALUES (14, 'lqt', 'lqt', 3, '张教授', 1, 'Java程序设计', 'ended', '4e0f549b40a24f53a26bda4eccbdddea', 'rtmp://localhost:8000/live/4e0f549b40a24f53a26bda4eccbdddea', 'http://localhost:8088/live/4e0f549b40a24f53a26bda4eccbdddea.m3u8', 'https://zh.wikipedia.org/wiki/%E7%A7%91%E6%AF%94%C2%B7%E5%B8%83%E8%8E%B1%E6%81%A9%E7%89%B9#/media/File:Kobe_Bryant_8.jpg', '2025-12-05 14:27:46', '2025-12-05 14:28:33', '2025-12-05 14:41:19', 2, 2, '2025-12-05 14:28:04', '2025-12-05 14:41:19');
INSERT INTO `live_streams` VALUES (16, 'lqt', 'lqt', 3, '张教授', NULL, NULL, 'ended', 'a8060d5c97b9408986771fa8ad310234', 'rtmp://localhost:8000/live/a8060d5c97b9408986771fa8ad310234', 'http://localhost:8088/live/a8060d5c97b9408986771fa8ad310234.m3u8', 'https://upload.wikimedia.org/wikipedia/commons/9/96/Kobe_Bryant_8.jpg', '2025-12-05 14:41:22', '2025-12-05 14:49:53', '2025-12-09 19:55:14', 35, 35, '2025-12-05 14:49:47', '2025-12-09 19:55:14');
INSERT INTO `live_streams` VALUES (18, 'lqt-1', 'lqt-1', 3, '张教授', 1, 'Java程序设计', 'ended', 'db5932217b1a4bc181230804feb4b21a', 'rtmp://localhost:8000/live/db5932217b1a4bc181230804feb4b21a', 'http://localhost:8088/live/db5932217b1a4bc181230804feb4b21a.m3u8', 'https://upload.wikimedia.org/wikipedia/commons/9/96/Kobe_Bryant_8.jpg', '2025-12-05 14:41:22', '2025-12-10 13:33:37', '2025-12-17 14:40:48', 13, 13, '2025-12-10 13:33:30', '2025-12-17 14:40:48');
INSERT INTO `live_streams` VALUES (19, 'xsz', 'nnnnnnnngggggggg', 3, '张教授', 3, '操作系统', 'ended', 'fd80647d45b549ccbb02606ade5303d0', 'rtmp://localhost:8000/live/fd80647d45b549ccbb02606ade5303d0', 'http://localhost:8088/live/fd80647d45b549ccbb02606ade5303d0.m3u8', NULL, '2025-12-17 00:00:00', '2025-12-17 14:41:24', '2025-12-17 15:06:48', 52, 52, '2025-12-17 14:41:19', '2025-12-17 15:06:48');

-- ----------------------------
-- Table structure for tb_admin
-- ----------------------------
DROP TABLE IF EXISTS `tb_admin`;
CREATE TABLE `tb_admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `admin_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '管理员编号',
  `permission_level` int NULL DEFAULT 1 COMMENT '权限等级(1-5)',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '数据查看员' COMMENT '角色名称',
  `permissions` json NULL COMMENT '自定义权限列表(可选)',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属部门',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_admin
-- ----------------------------
INSERT INTO `tb_admin` VALUES (1, 1, 'A001', 5, '超级管理员', NULL, NULL, NULL, NULL);
INSERT INTO `tb_admin` VALUES (2, 2, 'A002', 3, '教务管理员', NULL, '', NULL, NULL);
INSERT INTO `tb_admin` VALUES (3, 15, 'A003', 4, '日志管理员', NULL, '', NULL, NULL);
INSERT INTO `tb_admin` VALUES (4, 16, 'A004', 2, '资源管理员', NULL, '', NULL, NULL);

-- ----------------------------
-- Table structure for tb_admin_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `tb_admin_operation_log`;
CREATE TABLE `tb_admin_operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `admin_id` bigint NOT NULL COMMENT '管理员ID',
  `admin_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '管理员姓名',
  `permission_level` int NOT NULL COMMENT '操作时的权限等级',
  `operation` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作模块',
  `target_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标类型',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标ID',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求URL',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `status` enum('success','failed') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'success' COMMENT '操作状态',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息',
  `operation_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_admin_id`(`admin_id` ASC) USING BTREE,
  INDEX `idx_operation_time`(`operation_time` ASC) USING BTREE,
  INDEX `idx_module`(`module` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_admin_operation_log
-- ----------------------------

-- ----------------------------
-- Table structure for tb_admin_role
-- ----------------------------
DROP TABLE IF EXISTS `tb_admin_role`;
CREATE TABLE `tb_admin_role`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `permission_level` int NOT NULL COMMENT '权限等级(1-5)',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色描述',
  `permissions` json NOT NULL COMMENT '权限列表',
  `is_system` tinyint(1) NULL DEFAULT 0 COMMENT '是否系统角色',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_name`(`role_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员角色权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_admin_role
-- ----------------------------
INSERT INTO `tb_admin_role` VALUES (1, '超级管理员', 5, '系统最高权限,拥有所有操作权限', '[\"user:*\", \"course:*\", \"resource:*\", \"announcement:*\", \"system:*\", \"admin:*\"]', 1, '2025-12-23 18:31:35');
INSERT INTO `tb_admin_role` VALUES (2, '日志管理员', 4, '负责管理redis和所有日志', '[\"user:view\", \"course:view\", \"resource:view\", \"system:*\", \"monitor:*\"]', 1, '2025-12-23 18:31:35');
INSERT INTO `tb_admin_role` VALUES (3, '教务管理员', 3, '教务处工作人员,管理教学相关业务', '[\"user:*\", \"course:*\", \"resource:approve\", \"resource:delete\", \"announcement:*\", \"schedule:*\"]', 1, '2025-12-23 18:31:35');
INSERT INTO `tb_admin_role` VALUES (4, '资源管理员', 2, '内容审核人员,负责资源和公告审核', '[\"resource:view\", \"resource:approve\", \"resource:delete\", \"announcement:create\", \"announcement:edit\"]', 1, '2025-12-23 18:31:35');

-- ----------------------------
-- Table structure for tb_announcement
-- ----------------------------
DROP TABLE IF EXISTS `tb_announcement`;
CREATE TABLE `tb_announcement`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告内容',
  `type` enum('system','course','exam','other') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'system' COMMENT '公告类型',
  `priority` enum('low','medium','high') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'medium' COMMENT '优先级',
  `target_role` enum('all','student','teacher','admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'all' COMMENT '目标角色',
  `publisher_id` bigint NOT NULL COMMENT '发布者ID',
  `publisher_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发布者姓名(冗余)',
  `publish_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `status` enum('draft','published','archived') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'published' COMMENT '状态',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type_status`(`type` ASC, `status` ASC) USING BTREE,
  INDEX `idx_target_role`(`target_role` ASC) USING BTREE,
  INDEX `idx_publish_time`(`publish_time` DESC) USING BTREE,
  INDEX `idx_priority_status`(`priority` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_announcement
-- ----------------------------
INSERT INTO `tb_announcement` VALUES (1, '2025-2026学年第一学期选课通', '各位同学：\n\n本学期选课时间为11月20日-11月30日，请及时登录系统进行选课。\n\n教务处\n2025年11月15', 'course', 'high', 'student', 1, '系统管理员', '2025-11-26 22:33:04', 'published');
INSERT INTO `tb_announcement` VALUES (2, '系统维护通知', '系统将于本周六（11月23日）凌晨2:00-5:00进行维护升级，期间将无法访问，请各位用户合理安排时间。', 'system', 'medium', 'all', 1, '系统管理员', '2025-11-26 22:33:04', 'published');
INSERT INTO `tb_announcement` VALUES (4, '这是一个友好的测试', '超级哟好', 'system', 'medium', 'all', 1, '系统管理', '2025-12-05 08:51:51', 'published');

-- ----------------------------
-- Table structure for tb_code_submission
-- ----------------------------
DROP TABLE IF EXISTS `tb_code_submission`;
CREATE TABLE `tb_code_submission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '提交ID',
  `experiment_id` bigint NOT NULL COMMENT '实验ID',
  `experiment_problem_id` bigint NOT NULL COMMENT '实验题目ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `student_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学生姓名(冗余)',
  `language` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '编程语言(如 python3, java, cpp)',
  `code` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提交的源代码',
  `stdin` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '本次提交用于测试的输入(若为样例点则对应样例输入)',
  `expected_output` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '期望输出(用于样例比对)',
  `stdout` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '执行stdout',
  `stderr` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '执行stderr/错误信息',
  `piston_response` json NULL COMMENT 'Piston 原始响应(JSON)',
  `result` enum('AC','WA','TLE','RE','CE','PE','UNKNOWN') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'UNKNOWN' COMMENT '判题结果(AC/WA/超时/运行错误/编译错误/输出不完全)',
  `run_time_ms` int NULL DEFAULT NULL COMMENT '运行耗时(ms)',
  `memory_kb` int NULL DEFAULT NULL COMMENT '使用内存(kB)',
  `submit_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_experiment_problem`(`experiment_problem_id` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_result`(`result` ASC) USING BTREE,
  INDEX `idx_submit_time`(`submit_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '代码提交记录表(含Piston响应)' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_code_submission
-- ----------------------------
INSERT INTO `tb_code_submission` VALUES (1, 6, 1, 1, NULL, 'cpp', '#include <iostream>\nusing namespace std;\nint main() {\n  \n    int a, b, sum;\n    cin >> a; \n    cin >> b; \n    sum = a + b;\n    // 输出结果\n    cout <<<< sum << endl;\n    return 0;\n}', NULL, NULL, NULL, 'file0.code.cpp: In function \'int main()\':\nfile0.code.cpp:10:12: error: expected primary-expression before \'<<\' token\n   10 |     cout <<<< sum << endl;\n      |            ^~\nchmod: cannot access \'a.out\': No such file or directory\n', '{\"run\": {\"code\": 1, \"output\": \"file0.code.cpp: In function \'int main()\':\\nfile0.code.cpp:10:12: error: expected primary-expression before \'<<\' token\\n   10 |     cout <<<< sum << endl;\\n      |            ^~\\nchmod: cannot access \'a.out\': No such file or directory\\n\", \"signal\": null, \"stderr\": \"file0.code.cpp: In function \'int main()\':\\nfile0.code.cpp:10:12: error: expected primary-expression before \'<<\' token\\n   10 |     cout <<<< sum << endl;\\n      |            ^~\\nchmod: cannot access \'a.out\': No such file or directory\\n\", \"stdout\": \"\"}, \"compile\": {\"code\": 1, \"output\": \"file0.code.cpp: In function \'int main()\':\\nfile0.code.cpp:10:12: error: expected primary-expression before \'<<\' token\\n   10 |     cout <<<< sum << endl;\\n      |            ^~\\nchmod: cannot access \'a.out\': No such file or directory\\n\", \"signal\": null, \"stderr\": \"file0.code.cpp: In function \'int main()\':\\nfile0.code.cpp:10:12: error: expected primary-expression before \'<<\' token\\n   10 |     cout <<<< sum << endl;\\n      |            ^~\\nchmod: cannot access \'a.out\': No such file or directory\\n\", \"stdout\": \"\"}, \"version\": \"10.2.0\", \"language\": \"c++\"}', 'RE', NULL, NULL, '2025-11-27 09:04:04');
INSERT INTO `tb_code_submission` VALUES (2, 6, 1, 1, NULL, 'cpp', '#include <iostream>\nusing namespace std;\nint main() {\n  \n    int a, b, sum;\n    cin >> a; \n    cin >> b; \n    sum = a + b;\n    // 输出结果\n    cout << sum << endl;\n    return 0;\n}', NULL, NULL, NULL, NULL, '{\"run\": {\"code\": 0, \"output\": \"3\\n\", \"signal\": null, \"stderr\": \"\", \"stdout\": \"3\\n\"}, \"compile\": {\"code\": 0, \"output\": \"\", \"signal\": null, \"stderr\": \"\", \"stdout\": \"\"}, \"version\": \"10.2.0\", \"language\": \"c++\"}', 'AC', NULL, NULL, '2025-11-27 09:05:50');
INSERT INTO `tb_code_submission` VALUES (3, 6, 1, 1, NULL, 'cpp', '#include <iostream>\nusing namespace std;\nint main() {\n  \n    int a, b, sum;\n    cin >> a; \n    cin >> b; \n    sum = a + b;\n    // 输出结果\n    cout <<\"HELLO\"<< endl;\n    return 0;\n}', NULL, NULL, NULL, NULL, '{\"run\": {\"code\": 0, \"output\": \"HELLO\\n\", \"signal\": null, \"stderr\": \"\", \"stdout\": \"HELLO\\n\"}, \"compile\": {\"code\": 0, \"output\": \"\", \"signal\": null, \"stderr\": \"\", \"stdout\": \"\"}, \"version\": \"10.2.0\", \"language\": \"c++\"}', 'WA', NULL, NULL, '2025-11-27 09:15:23');
INSERT INTO `tb_code_submission` VALUES (4, 6, 1, 1, NULL, 'cpp', '#include <iostream>\nusing namespace std;\n\nint main() {\n    double a, b;  // 用double兼顾整数和浮点数求和\n    cin >> a >> b;  // 直接读取输入的两个数（空格/回车分隔）\n    cout << a + b << endl;  // 直接输出和，无任何多余提示\n    return 0;\n}', NULL, NULL, NULL, NULL, '{\"run\": {\"code\": 0, \"output\": \"3\\n\", \"signal\": null, \"stderr\": \"\", \"stdout\": \"3\\n\"}, \"compile\": {\"code\": 0, \"output\": \"\", \"signal\": null, \"stderr\": \"\", \"stdout\": \"\"}, \"version\": \"10.2.0\", \"language\": \"c++\"}', 'AC', NULL, NULL, '2025-11-27 10:17:07');
INSERT INTO `tb_code_submission` VALUES (5, 6, 1, 1, NULL, 'java', 'import java.util.Scanner;\n\npublic class Sum {\n    public static void main(String[] args) {\n        // 创建Scanner对象读取控制台输入\n        Scanner scanner = new Scanner(System.in);\n        // 读取两个数（支持整数和浮点数，用double类型兼容）\n        double a = scanner.nextDouble();\n        double b = scanner.nextDouble();\n        // 直接输出两数之和\n        System.out.println(a + b);\n        // 关闭Scanner（可选，控制台程序不关闭也不影响）\n        scanner.close();\n    }\n}', NULL, NULL, NULL, NULL, '{\"run\": {\"code\": 0, \"output\": \"3.0\\n\", \"signal\": null, \"stderr\": \"\", \"stdout\": \"3.0\\n\"}, \"version\": \"15.0.2\", \"language\": \"java\"}', 'WA', NULL, NULL, '2025-11-27 10:19:10');
INSERT INTO `tb_code_submission` VALUES (6, 6, 1, 1, NULL, 'java', 'import java.util.Scanner;\n\npublic class Sum {\n    public static void main(String[] args) {\n        // 创建Scanner对象读取控制台输入\n        Scanner scanner = new Scanner(System.in);\n        // 读取两个数（支持整数和浮点数，用double类型兼容）\n       int a = (int) scanner.nextDouble();\n        int b = (int) scanner.nextDouble();\n        // 直接输出两数之和\n        System.out.println(a + b);\n        // 关闭Scanner（可选，控制台程序不关闭也不影响）\n        scanner.close();\n    }\n}', NULL, NULL, NULL, NULL, '{\"run\": {\"code\": 0, \"output\": \"3\\n\", \"signal\": null, \"stderr\": \"\", \"stdout\": \"3\\n\"}, \"version\": \"15.0.2\", \"language\": \"java\"}', 'AC', NULL, NULL, '2025-11-27 10:23:01');
INSERT INTO `tb_code_submission` VALUES (7, 7, 2, 1, NULL, 'java', '1111\n', NULL, NULL, NULL, 'file0.code.java:1: error: class, interface, or enum expected\n1111\n^\n1 error\nerror: compilation failed\n', '{\"run\": {\"code\": 1, \"output\": \"file0.code.java:1: error: class, interface, or enum expected\\n1111\\n^\\n1 error\\nerror: compilation failed\\n\", \"signal\": null, \"stderr\": \"file0.code.java:1: error: class, interface, or enum expected\\n1111\\n^\\n1 error\\nerror: compilation failed\\n\", \"stdout\": \"\"}, \"version\": \"15.0.2\", \"language\": \"java\"}', 'RE', NULL, NULL, '2025-11-27 14:37:04');
INSERT INTO `tb_code_submission` VALUES (8, 7, 2, 1, NULL, 'java', '234535', NULL, NULL, NULL, 'file0.code.java:1: error: class, interface, or enum expected\n234535\n^\n1 error\nerror: compilation failed\n', '{\"run\": {\"code\": 1, \"output\": \"file0.code.java:1: error: class, interface, or enum expected\\n234535\\n^\\n1 error\\nerror: compilation failed\\n\", \"signal\": null, \"stderr\": \"file0.code.java:1: error: class, interface, or enum expected\\n234535\\n^\\n1 error\\nerror: compilation failed\\n\", \"stdout\": \"\"}, \"version\": \"15.0.2\", \"language\": \"java\"}', 'RE', NULL, NULL, '2025-11-27 14:40:26');
INSERT INTO `tb_code_submission` VALUES (9, 6, 1, 1, NULL, 'java', '深爱的', NULL, NULL, NULL, 'file0.code.java:1: error: illegal character: \'\\ufffd\'\n?????????\n^\nfile0.code.java:1: error: illegal character: \'\\ufffd\'\n?????????\n ^\nfile0.code.java:1: error: illegal character: \'\\ufffd\'\n?????????\n  ^\nfile0.code.java:1: error: illegal character: \'\\ufffd\'\n?????????\n   ^\nfile0.code.java:1: error: illegal character: \'\\ufffd\'\n?????????\n    ^\nfile0.code.java:1: error: illegal character: \'\\ufffd\'\n?????????\n     ^\nfile0.code.java:1: error: illegal character: \'\\ufffd\'\n?????????\n      ^\nfile0.code.java:1: error: illegal character: \'\\ufffd\'\n?????????\n       ^\nfile0.code.java:1: error: illegal character: \'\\ufffd\'\n?????????\n        ^\nfile0.code.java:1: error: reached end of file while parsing\n?????????\n         ^\n10 errors\nerror: compilation failed\n', '{\"run\": {\"code\": 1, \"output\": \"file0.code.java:1: error: illegal character: \'\\\\ufffd\'\\n?????????\\n^\\nfile0.code.java:1: error: illegal character: \'\\\\ufffd\'\\n?????????\\n ^\\nfile0.code.java:1: error: illegal character: \'\\\\ufffd\'\\n?????????\\n  ^\\nfile0.code.java:1: error: illegal character: \'\\\\ufffd\'\\n?????????\\n   ^\\nfile0.code.java:1: error: illegal character: \'\\\\ufffd\'\\n?????????\\n    ^\\nfile0.code.java:1: error: illegal character: \'\\\\ufffd\'\\n?????????\\n     ^\\nfile0.code.java:1: error: illegal character: \'\\\\ufffd\'\\n?????????\\n      ^\\nfile0.code.java:1: error: illegal character: \'\\\\ufffd\'\\n?????????\\n       ^\\nfile0.code.java:1: error: illegal character: \'\\\\ufffd\'\\n?????????\\n        ^\\nfile0.code.java:1: error: reached end of file while parsing\\n?????????\\n         ^\\n10 errors\\nerror: compilation failed\\n\", \"signal\": null, \"stderr\": \"file0.code.java:1: error: illegal character: \'\\\\ufffd\'\\n?????????\\n^\\nfile0.code.java:1: error: illegal character: \'\\\\ufffd\'\\n?????????\\n ^\\nfile0.code.java:1: error: illegal character: \'\\\\ufffd\'\\n?????????\\n  ^\\nfile0.code.java:1: error: illegal character: \'\\\\ufffd\'\\n?????????\\n   ^\\nfile0.code.java:1: error: illegal character: \'\\\\ufffd\'\\n?????????\\n    ^\\nfile0.code.java:1: error: illegal character: \'\\\\ufffd\'\\n?????????\\n     ^\\nfile0.code.java:1: error: illegal character: \'\\\\ufffd\'\\n?????????\\n      ^\\nfile0.code.java:1: error: illegal character: \'\\\\ufffd\'\\n?????????\\n       ^\\nfile0.code.java:1: error: illegal character: \'\\\\ufffd\'\\n?????????\\n        ^\\nfile0.code.java:1: error: reached end of file while parsing\\n?????????\\n         ^\\n10 errors\\nerror: compilation failed\\n\", \"stdout\": \"\"}, \"version\": \"15.0.2\", \"language\": \"java\"}', 'RE', NULL, NULL, '2025-11-27 14:45:23');
INSERT INTO `tb_code_submission` VALUES (10, 6, 1, 1, NULL, 'go', 'package main\n\nimport (\n	\"bufio\"\n	\"fmt\"\n	\"os\"\n	\"strconv\"\n	\"strings\"\n)\n\nfunc main() {\n	// 创建扫描器读取标准输入\n	scanner := bufio.NewScanner(os.Stdin)\n	// 读取一行输入（支持空格分隔的a和b）\n	scanner.Scan()\n	input := scanner.Text()\n\n	// 分割输入内容为两个部分（按空格/制表符等空白符分割）\n	parts := strings.Fields(input)\n	if len(parts) != 2 {\n		// 输入格式错误时无多余输出（仅静默处理，符合“无多余提示”要求）\n		return\n	}\n\n	// 解析第一个数\n	num1, err1 := strconv.ParseFloat(parts[0], 64)\n	// 解析第二个数\n	num2, err2 := strconv.ParseFloat(parts[1], 64)\n	if err1 != nil || err2 != nil {\n		// 解析失败时无多余输出\n		return\n	}\n\n	// 输出两数之和（无任何多余字符）\n	fmt.Println(num1 + num2)\n}', NULL, NULL, NULL, NULL, '{\"run\": {\"code\": 0, \"output\": \"3\\n\", \"signal\": null, \"stderr\": \"\", \"stdout\": \"3\\n\"}, \"version\": \"1.16.2\", \"language\": \"go\"}', 'AC', NULL, NULL, '2025-12-04 15:14:38');
INSERT INTO `tb_code_submission` VALUES (11, 6, 1, 1, NULL, 'go', 'package main\n\nimport (\n	\"bufio\"\n	\"fmt\"\n	\"os\"\n	\"strconv\"\n	\"strings\"\n)\n\nfunc main() {\n	// 创建扫描器读取标准输入\n	scanner := bufio.NewScanner(os.Stdin)\n	// 读取一行输入（支持空格分隔的a和b）\n	scanner.Scan()\n	input := scanner.Text()\n\n	// 分割输入内容为两个部分（按空格/制表符等空白符分割）\n	parts := strings.Fields(input)\n	if len(parts) != 2 {\n		// 输入格式错误时无多余输出（仅静默处理，符合“无多余提示”要求）\n		return\n	}\n\n	// 解析第一个数\n	num1, err1 := strconv.ParseFloat(parts[0], 64)\n	// 解析第二个数\n	num2, err2 := strconv.ParseFloat(parts[1], 64)\n	if err1 != nil || err2 != nil {\n		// 解析失败时无多余输出\n		return\n	}\n\n	// 输出两数之和（无任何多余字符）\n	fmt.Println(num1 + num2)\n}', NULL, NULL, NULL, NULL, '{\"run\": {\"code\": 0, \"output\": \"3\\n\", \"signal\": null, \"stderr\": \"\", \"stdout\": \"3\\n\"}, \"version\": \"1.16.2\", \"language\": \"go\"}', 'AC', NULL, NULL, '2025-12-04 16:14:27');
INSERT INTO `tb_code_submission` VALUES (12, 6, 1, 1, NULL, 'go', 'package main\n\nimport (\n	\"bufio\"\n	\"fmt\"\n	\"os\"\n	\"strconv\"\n	\"strings\"\n)\n\nfunc main() {\n	// 创建扫描器读取标准输入\n	scanner := bufio.NewScanner(os.Stdin)\n	// 读取一行输入（支持空格分隔的a和b）\n	scanner.Scan()\n	input := scanner.Text()\n\n	// 分割输入内容为两个部分（按空格/制表符等空白符分割）\n	parts := strings.Fields(input)\n	if len(parts) != 2 {\n		// 输入格式错误时无多余输出（仅静默处理，符合“无多余提示”要求）\n		return\n\n	// 解析第一个数\n	num1, err1 := strconv.ParseFloat(parts[0], 64)\n	// 解析第二个数\n	num2, err2 := strconv.ParseFloat(parts[1], 64)\n	if err1 != nil || err2 != nil {\n		// 解析失败时无多余输出\n		return\n	}\n\n	// 输出两数之和（无任何多余字符）\n	fmt.Println(num1 + num2)\n}', NULL, NULL, NULL, '# command-line-arguments\n./file0.code.go:35:2: syntax error: unexpected EOF, expecting }\n', '{\"run\": {\"code\": 2, \"output\": \"# command-line-arguments\\n./file0.code.go:35:2: syntax error: unexpected EOF, expecting }\\n\", \"signal\": null, \"stderr\": \"# command-line-arguments\\n./file0.code.go:35:2: syntax error: unexpected EOF, expecting }\\n\", \"stdout\": \"\"}, \"version\": \"1.16.2\", \"language\": \"go\"}', 'RE', NULL, NULL, '2025-12-04 16:14:45');
INSERT INTO `tb_code_submission` VALUES (13, 6, 1, 1, NULL, 'python2', '# 读取标准输入并分割为两个数值\ninput_str = input().strip()\nnums = input_str.split()\n\n# 解析为浮点数（兼容整数和小数）并计算和\na = float(nums[0])\nb = float(nums[1])\nsum_result = a + b\n\n# 输出结果（无多余字符）\nprint(sum_result)', NULL, NULL, NULL, '  File \"file0.code\", line 1\nSyntaxError: Non-ASCII character \'\\xe8\' in file file0.code on line 1, but no encoding declared; see http://python.org/dev/peps/pep-0263/ for details\n', '{\"run\": {\"code\": 1, \"output\": \"  File \\\"file0.code\\\", line 1\\nSyntaxError: Non-ASCII character \'\\\\xe8\' in file file0.code on line 1, but no encoding declared; see http://python.org/dev/peps/pep-0263/ for details\\n\", \"signal\": null, \"stderr\": \"  File \\\"file0.code\\\", line 1\\nSyntaxError: Non-ASCII character \'\\\\xe8\' in file file0.code on line 1, but no encoding declared; see http://python.org/dev/peps/pep-0263/ for details\\n\", \"stdout\": \"\"}, \"version\": \"2.7.18\", \"language\": \"python2\"}', 'RE', NULL, NULL, '2025-12-04 16:22:49');
INSERT INTO `tb_code_submission` VALUES (14, 6, 1, 1, NULL, 'python', 'fpackage cn.edu.zjut.back.entity;\n\nimport lombok.Data;\nimport java.time.LocalDateTime;\n\n/**\n * 讨论回复实体类 (tb_discussion_reply)\n */\n@Data\npublic class DiscussionReply {\n    private Long id;\n    private Long discussionId;  // discussion_id\n    private Long parentId;  // parent_id\n    private String content;\n    private Long authorId;  // author_id\n    private String authorName;  // author_name\n    private String authorRole;  // ENUM(\'student\', \'teacher\', \'admin\')\n    private Boolean isAccepted;  // is_accepted (TINYINT(1))\n    private Integer likeCount;  // like_count\n    private String status;  // ENUM(\'active\', \'hidden\', \'deleted\')\n    private LocalDateTime createTime;\n    private LocalDateTime updateTime;\n}\npackage cn.edu.zjut.back.entity;\n\nimport lombok.Data;\nimport java.time.LocalDateTime;\n\n/**\n * 讨论回复实体类 (tb_discussion_reply)\n */\n@Data\npublic class DiscussionReply {\n    private Long id;\n    private Long discussionId;  // discussion_id\n    private Long parentId;  // parent_id\n    private String content;\n    private Long authorId;  // author_id\n    private String authorName;  // author_name\n    private String authorRole;  // ENUM(\'student\', \'teacher\', \'admin\')\n    private Boolean isAccepted;  // is_accepted (TINYINT(1))\n    private Integer likeCount;  // like_count\n    private String status;  // ENUM(\'active\', \'hidden\', \'deleted\')\n    private LocalDateTime createTime;\n    private LocalDateTime updateTime;\n}\n', NULL, NULL, NULL, '  File \"/piston/jobs/1727f1b4-f0f8-4042-a49e-a92707f9f0bd/file0.code\", line 1\n    fpackage cn.edu.zjut.back.entity;\n    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\nSyntaxError: invalid syntax. Perhaps you forgot a comma?\n', '{\"run\": {\"code\": 1, \"output\": \"  File \\\"/piston/jobs/1727f1b4-f0f8-4042-a49e-a92707f9f0bd/file0.code\\\", line 1\\n    fpackage cn.edu.zjut.back.entity;\\n    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\\nSyntaxError: invalid syntax. Perhaps you forgot a comma?\\n\", \"signal\": null, \"stderr\": \"  File \\\"/piston/jobs/1727f1b4-f0f8-4042-a49e-a92707f9f0bd/file0.code\\\", line 1\\n    fpackage cn.edu.zjut.back.entity;\\n    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\\nSyntaxError: invalid syntax. Perhaps you forgot a comma?\\n\", \"stdout\": \"\"}, \"version\": \"3.10.0\", \"language\": \"python\"}', 'RE', NULL, NULL, '2025-12-04 16:37:14');

-- ----------------------------
-- Table structure for tb_course
-- ----------------------------
DROP TABLE IF EXISTS `tb_course`;
CREATE TABLE `tb_course`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `course_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程代码',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名称',
  `teacher_id` bigint NOT NULL COMMENT '授课教师ID',
  `teacher_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '教师姓名(冗余)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '课程描述',
  `semester` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学期(如2025-2026-1)',
  `credit` decimal(3, 1) NOT NULL COMMENT '学分',
  `capacity` int NOT NULL COMMENT '容量',
  `enrolled` int NULL DEFAULT 0 COMMENT '已选人数',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '课程类别',
  `status` enum('pending','approved','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '课程状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_course_code`(`course_code` ASC) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_semester_status`(`semester` ASC, `status` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_course
-- ----------------------------
INSERT INTO `tb_course` VALUES (1, 'CS101', 'Java程序设计', 3, '张教授', 'Java语言基础及面向对象编程', '2025-2026-1', 4.0, 50, 36, '专业必修', 'approved', '2025-11-26 22:33:04', '2025-12-04 23:40:14');
INSERT INTO `tb_course` VALUES (2, 'CS102', 'Web开发技术', 5, '王讲师', '前后端Web应用开发', '2025-2026-1', 3.0, 40, 1, '专业选修', 'approved', '2025-11-26 22:33:04', '2025-12-04 23:40:14');
INSERT INTO `tb_course` VALUES (3, 'CS103', '操作系统', 3, '张教授', '操作系统原理与实践', '2025-2026-1', 4.0, 50, 39, '专业必修', 'approved', '2025-11-26 22:33:04', '2025-12-04 23:40:14');
INSERT INTO `tb_course` VALUES (4, 'CS104', '数据结构', 3, '张教授', '数据结构与算法设计', '2025-2026-1', 4.0, 50, 41, '专业必修', 'approved', '2025-11-26 22:33:04', '2025-12-04 23:40:14');
INSERT INTO `tb_course` VALUES (5, 'CS105', '数据库原理', 4, '李老师', '关系型数据库设计与SQL', '2025-2026-1', 3.5, 60, 46, '专业必修', 'approved', '2025-11-26 22:33:04', '2025-12-04 23:40:14');
INSERT INTO `tb_course` VALUES (6, 'CS106', '计算机网络', 5, '王讲师', '计算机网络原理、协议与应用', '2025-2026-1', 3.5, 45, 29, '专业必修', 'approved', '2025-12-01 16:45:38', '2025-12-04 23:40:14');
INSERT INTO `tb_course` VALUES (7, 'CS107', '软件工程', 3, '张教授', '软件开发流程、设计模式与项目管理', '2025-2026-1', 3.0, 40, 32, '专业必修', 'approved', '2025-12-01 16:45:38', '2025-12-04 23:40:14');
INSERT INTO `tb_course` VALUES (8, 'CS108', '人工智能导论', 4, '李老师', 'AI基础理论与Python实战', '2025-2026-1', 3.0, 35, 21, '专业选修', 'approved', '2025-12-01 16:45:38', '2025-12-09 19:48:57');
INSERT INTO `tb_course` VALUES (9, 'CS109', '移动应用开发', 5, '王讲师', 'Android/iOS移动端开发技术', '2025-2026-1', 2.5, 30, 18, '专业选修', 'approved', '2025-12-01 16:45:38', '2025-12-04 23:40:14');
INSERT INTO `tb_course` VALUES (10, 'CS110', '大数据技术', 4, '李老师', 'Hadoop、Spark大数据处理', '2025-2026-1', 3.0, 35, 25, '专业选修', 'approved', '2025-12-01 16:45:38', '2025-12-04 23:40:14');
INSERT INTO `tb_course` VALUES (11, 'CS111', '云计算技术', 5, '王讲师', '云计算架构与容器技术', '2025-2026-1', 2.5, 30, 15, '专业选修', 'approved', '2025-12-01 16:45:38', '2025-12-04 23:40:14');
INSERT INTO `tb_course` VALUES (12, 'CS112', '信息安全', 3, '张教授', '网络安全、密码学与安全防护', '2025-2026-1', 3.0, 40, 30, '专业选修', 'approved', '2025-12-01 16:45:38', '2025-12-04 23:40:14');
INSERT INTO `tb_course` VALUES (13, 'MA201', '高等数学A', 3, '张教授', '微积分、级数与多元函数', '2025-2026-1', 5.0, 60, 55, '公共必修', 'approved', '2025-12-01 16:45:38', '2025-12-04 23:40:14');
INSERT INTO `tb_course` VALUES (14, 'EN201', '大学英语', 4, '李老师', '英语听说读写综合训练', '2025-2026-1', 3.0, 50, 48, '公共必修', 'approved', '2025-12-01 16:45:38', '2025-12-04 23:40:14');
INSERT INTO `tb_course` VALUES (15, 'PE201', '体王', 5, '王讲师', '体育锻炼与健康管理', '2025-2026-1', 1.0, 41, 35, '公共必修', 'approved', '2025-12-01 16:45:38', '2025-12-04 23:40:14');

-- ----------------------------
-- Table structure for tb_course_selection
-- ----------------------------
DROP TABLE IF EXISTS `tb_course_selection`;
CREATE TABLE `tb_course_selection`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '选课记录ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学生姓名(冗余)',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名称(冗余)',
  `select_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
  `status` enum('selected','dropped','completed') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'selected' COMMENT '选课状态',
  `final_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '最终成绩',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_course`(`student_id` ASC, `course_id` ASC) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_student_status`(`student_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '选课记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_course_selection
-- ----------------------------
INSERT INTO `tb_course_selection` VALUES (1, 1, '张三', 1, 'Java程序设计', '2025-11-26 22:33:04', 'selected', NULL);
INSERT INTO `tb_course_selection` VALUES (2, 1, '张三', 5, '数据库原理', '2025-11-26 22:33:04', 'selected', NULL);
INSERT INTO `tb_course_selection` VALUES (3, 2, '李四', 1, 'Java程序设计', '2025-11-26 22:33:04', 'selected', NULL);
INSERT INTO `tb_course_selection` VALUES (4, 2, '李四', 4, '数据结构', '2025-11-26 22:33:04', 'selected', NULL);
INSERT INTO `tb_course_selection` VALUES (5, 3, '王五', 5, '数据库原理', '2025-11-26 22:33:04', 'selected', NULL);
INSERT INTO `tb_course_selection` VALUES (6, 3, '王五', 2, 'Web开发技术', '2025-11-26 22:33:04', 'selected', NULL);
INSERT INTO `tb_course_selection` VALUES (7, 6, '张三', 1, 'Java程序设计', '2025-11-26 23:10:19', 'selected', NULL);
INSERT INTO `tb_course_selection` VALUES (8, 6, '张三', 3, '操作系统', '2025-12-01 16:50:02', 'selected', NULL);
INSERT INTO `tb_course_selection` VALUES (9, 6, '张三', 4, '数据结构', '2025-12-01 16:50:02', 'selected', NULL);
INSERT INTO `tb_course_selection` VALUES (10, 6, '张三', 6, '计算机网络', '2025-12-01 16:50:02', 'selected', NULL);
INSERT INTO `tb_course_selection` VALUES (11, 6, '张三', 8, '人工智能导论', '2025-12-09 19:48:56', 'selected', NULL);

-- ----------------------------
-- Table structure for tb_discussion
-- ----------------------------
DROP TABLE IF EXISTS `tb_discussion`;
CREATE TABLE `tb_discussion`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '讨论ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名称(冗余)',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '讨论标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '讨论内容',
  `topic_type` enum('question','discussion','sharing','notice') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'question' COMMENT '话题类型',
  `author_id` bigint NOT NULL COMMENT '发布者ID',
  `author_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发布者姓名',
  `author_role` enum('student','teacher','admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发布者角色',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览次数',
  `reply_count` int NULL DEFAULT 0 COMMENT '回复数量',
  `is_pinned` tinyint(1) NULL DEFAULT 0 COMMENT '是否置顶',
  `is_resolved` tinyint(1) NULL DEFAULT 0 COMMENT '是否已解决',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签',
  `status` enum('active','hidden','deleted') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active' COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_author_id`(`author_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` DESC) USING BTREE,
  INDEX `idx_course_status`(`course_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_topic_type`(`topic_type` ASC) USING BTREE,
  INDEX `idx_pinned_time`(`is_pinned` ASC, `create_time` DESC) USING BTREE,
  INDEX `idx_reply_count`(`reply_count` DESC) USING BTREE,
  FULLTEXT INDEX `idx_fulltext_title_content`(`title`, `content`)
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '在线讨论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_discussion
-- ----------------------------
INSERT INTO `tb_discussion` VALUES (1, 1, 'Java程序设计', 'Java多线程如何实现?', '我在学习Java多线程时遇到了一些困惑,请问如何正确创建和使用线程?有哪些方式?', 'question', 6, '张三', 'student', 45, 4, 0, 1, 'Java,多线程', 'active', '2025-11-26 22:33:04', '2025-12-04 22:02:07');
INSERT INTO `tb_discussion` VALUES (2, 1, 'Java程序设计', 'Java集合框架学习资料分享', '分享一些我整理的Java集合框架学习资料和思维导图,希望对大家有帮助。', 'sharing', 7, '李四', 'student', 68, 7, 0, 0, 'Java,集合框架', 'active', '2025-11-26 22:33:04', '2025-12-04 22:02:07');
INSERT INTO `tb_discussion` VALUES (3, 5, '数据库原理', '关于数据库索引的问题', '什么时候应该创建索引?索引会影响性能吗?', 'question', 3, '王五', 'student', 32, 2, 0, 0, '数据库,索引', 'active', '2025-11-26 22:33:04', '2025-11-26 22:33:04');

-- ----------------------------
-- Table structure for tb_discussion_reply
-- ----------------------------
DROP TABLE IF EXISTS `tb_discussion_reply`;
CREATE TABLE `tb_discussion_reply`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '回复ID',
  `discussion_id` bigint NOT NULL COMMENT '讨论ID',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父回复ID(支持多级回复)',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回复内容',
  `author_id` bigint NOT NULL COMMENT '回复者ID',
  `author_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回复者姓名',
  `author_role` enum('student','teacher','admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '回复者角色',
  `is_accepted` tinyint(1) NULL DEFAULT 0 COMMENT '是否被采纳(最佳答案)',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `status` enum('active','hidden','deleted') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active' COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_discussion_id`(`discussion_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_author_id`(`author_id` ASC) USING BTREE,
  INDEX `idx_discussion_status`(`discussion_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_accepted`(`is_accepted` ASC) USING BTREE,
  INDEX `idx_like_count`(`like_count` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '讨论回复表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_discussion_reply
-- ----------------------------
INSERT INTO `tb_discussion_reply` VALUES (1, 1, NULL, 'Java实现多线程主要有三种方式:1)继承Thread类 2)实现Runnable接口 3)实现Callable接口。推荐使用接口方式,因为Java不支持多继承。', 3, '张教授', 'teacher', 1, 19, 'active', '2025-11-26 22:33:04', '2025-12-10 14:37:58');
INSERT INTO `tb_discussion_reply` VALUES (2, 1, 1, '老师讲得很清楚,谢谢!那请问线程池又是什么呢?', 6, '张三', 'student', 0, 3, 'active', '2025-11-26 22:33:04', '2025-12-04 22:02:07');
INSERT INTO `tb_discussion_reply` VALUES (3, 1, 2, '线程池是一种线程复用技术,可以避免频繁创建和销毁线程的开销。Java提供了ExecutorService接口和Executors工具类来使用线程池。', 3, '张教授', 'teacher', 1, NULL, 'active', '2025-11-26 22:33:04', '2025-12-04 22:02:07');
INSERT INTO `tb_discussion_reply` VALUES (4, 2, NULL, '非常好的资料,收藏了!', 6, '张三', 'student', 0, 5, 'active', '2025-11-26 22:33:04', '2025-12-04 22:02:07');
INSERT INTO `tb_discussion_reply` VALUES (5, 2, NULL, 'HashMap的实现原理也可以补充一下', 8, '王五', 'student', 0, 2, 'active', '2025-11-26 22:33:04', '2025-12-04 22:02:07');

-- ----------------------------
-- Table structure for tb_exam_paper
-- ----------------------------
DROP TABLE IF EXISTS `tb_exam_paper`;
CREATE TABLE `tb_exam_paper`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '试卷ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名称(冗余)',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '试卷标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '试卷说明',
  `total_score` int NOT NULL DEFAULT 100 COMMENT '试卷总分',
  `pass_score` int NOT NULL DEFAULT 60 COMMENT '及格分数',
  `duration` int NOT NULL COMMENT '考试时长(分钟)',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `exam_type` enum('online','offline') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'online' COMMENT '考试类型',
  `shuffle_questions` tinyint(1) NULL DEFAULT 0 COMMENT '是否打乱题目顺序',
  `shuffle_options` tinyint(1) NULL DEFAULT 0 COMMENT '是否打乱选项顺序',
  `show_answer` tinyint(1) NULL DEFAULT 0 COMMENT '是否显示答案',
  `allow_review` tinyint(1) NULL DEFAULT 1 COMMENT '是否允许查看试卷',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `creator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创建者姓名(冗余)',
  `status` enum('draft','published','closed','archived') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'draft' COMMENT '试卷状态',
  `paper_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '试卷内容JSON文件URL（用于学生端加载）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_start_end_time`(`start_time` ASC, `end_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '试卷表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_exam_paper
-- ----------------------------
INSERT INTO `tb_exam_paper` VALUES (1, 1, 'Java程序设计', 'Java程序设计期中考试', '本次考试涵盖Java基础语法、面向对象编程、异常处理等内容', 100, 60, 90, '2025-12-02 09:00:00', '2025-12-05 23:59:00', 'online', 0, 0, 1, 1, 3, '张教授', 'published', 'http://120.26.212.210/resources/exam/exam_demo_1.json', '2025-12-02 22:45:02', '2025-12-03 01:05:16');
INSERT INTO `tb_exam_paper` VALUES (2, 2, '数据结构', '数据结构与算法测验', '本次测验包括线性表、树、图、排序和查找算法等内容', 100, 60, 60, '2025-12-03 14:00:00', '2025-12-06 23:59:00', 'online', 0, 1, 1, 1, 4, '李老师', 'published', 'http://120.26.212.210/resources/exam/exam_demo_2.json', '2025-12-02 22:45:06', '2025-12-03 01:05:16');
INSERT INTO `tb_exam_paper` VALUES (3, 1, 'Java程序设计', 'Java程序设计测试考试', '本次考试涵盖Java基础语法、面向对象编程、异常处理等内容', 100, 60, 90, '2025-12-02 09:00:00', '2025-12-30 23:59:00', 'online', 0, 0, 1, 1, 3, '张教授', 'published', 'http://120.26.212.210/resources/exam/exam_demo_3.json', '2025-12-03 00:52:57', '2025-12-03 01:05:16');

-- ----------------------------
-- Table structure for tb_exam_question
-- ----------------------------
DROP TABLE IF EXISTS `tb_exam_question`;
CREATE TABLE `tb_exam_question`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `exam_paper_id` bigint NOT NULL COMMENT '试卷ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `question_order` int NOT NULL COMMENT '题目序号',
  `question_score` int NOT NULL COMMENT '本题分值',
  `is_required` tinyint(1) NULL DEFAULT 1 COMMENT '是否必答',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_exam_question`(`exam_paper_id` ASC, `question_id` ASC) USING BTREE,
  INDEX `idx_exam_paper_id`(`exam_paper_id` ASC) USING BTREE,
  INDEX `idx_question_id`(`question_id` ASC) USING BTREE,
  INDEX `idx_order`(`exam_paper_id` ASC, `question_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '试卷题目关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_exam_question
-- ----------------------------
INSERT INTO `tb_exam_question` VALUES (1, 1, 1, 1, 5, 1);
INSERT INTO `tb_exam_question` VALUES (2, 1, 2, 2, 5, 1);
INSERT INTO `tb_exam_question` VALUES (3, 1, 3, 3, 10, 1);
INSERT INTO `tb_exam_question` VALUES (4, 1, 4, 4, 5, 1);
INSERT INTO `tb_exam_question` VALUES (5, 1, 5, 5, 10, 1);
INSERT INTO `tb_exam_question` VALUES (6, 1, 6, 6, 20, 1);
INSERT INTO `tb_exam_question` VALUES (7, 1, 7, 7, 25, 1);
INSERT INTO `tb_exam_question` VALUES (8, 2, 2, 1, 10, 1);
INSERT INTO `tb_exam_question` VALUES (9, 2, 3, 2, 15, 1);
INSERT INTO `tb_exam_question` VALUES (10, 2, 6, 3, 25, 1);
INSERT INTO `tb_exam_question` VALUES (11, 3, 8, 1, 5, 1);
INSERT INTO `tb_exam_question` VALUES (12, 3, 9, 2, 5, 1);
INSERT INTO `tb_exam_question` VALUES (13, 3, 10, 3, 10, 1);
INSERT INTO `tb_exam_question` VALUES (14, 3, 11, 4, 20, 1);
INSERT INTO `tb_exam_question` VALUES (15, 3, 12, 5, 20, 1);

-- ----------------------------
-- Table structure for tb_experiment
-- ----------------------------
DROP TABLE IF EXISTS `tb_experiment`;
CREATE TABLE `tb_experiment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '实验ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名称(冗余)',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '实验标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '实验描述',
  `requirements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '实验要求',
  `steps` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '实验步骤',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `deadline` datetime NOT NULL COMMENT '截止时间',
  `environment_type` enum('cloud','local') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'cloud' COMMENT '环境类型',
  `environment_config` json NULL COMMENT '环境配置(JSON对象)',
  `resources` json NULL COMMENT '实验资源(JSON数组)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_deadline`(`deadline` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '实验表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_experiment
-- ----------------------------
INSERT INTO `tb_experiment` VALUES (1, 1, 'Java程序设计', 'Java开发环境搭建', '配置Java开发环境并编写Hello World程序', '1. 安装JDK\n2. 配置环境变量\n3. 使用IDE编写程序', NULL, NULL, '2025-11-30 23:59:59', 'local', NULL, NULL, '2025-11-26 22:33:04', '2025-11-26 22:33:04');
INSERT INTO `tb_experiment` VALUES (2, 1, 'Java程序设计', 'Java集合框架实验', '熟练掌握Java集合框架的使用', '1. ArrayList使用\n2. HashMap使用\n3. 性能对比分析', NULL, NULL, '2025-12-10 23:59:59', 'cloud', '{\"cpu\": \"2核\", \"image\": \"openjdk:17\", \"memory\": \"4GB\"}', NULL, '2025-11-26 22:33:04', '2025-11-26 22:33:04');
INSERT INTO `tb_experiment` VALUES (3, 5, '数据库原理', 'MySQL数据库实验', '创建数据库和表，执行CRUD操作', '1. 创建数据库\n2. 设计表结构\n3. 数据操作', NULL, NULL, '2025-12-08 23:59:59', 'cloud', '{\"cpu\": \"2核\", \"image\": \"mysql:8.0\", \"memory\": \"4GB\"}', NULL, '2025-11-26 22:33:04', '2025-11-26 22:33:04');
INSERT INTO `tb_experiment` VALUES (6, 1, 'Java程序设计', '发的', '艾弗森', '阿萨', ' 暗室逢灯', '2025-11-26 16:00:00', '2025-11-27 16:00:00', 'local', '{}', '[]', '2025-11-27 08:21:43', '2025-11-27 08:21:43');
INSERT INTO `tb_experiment` VALUES (7, 2, 'Web开发技术', '阿斯蒂芬', '轻微发热他', '去微软', '去微软', '2025-11-27 16:00:00', '2025-12-03 16:00:00', 'local', '{}', '[]', '2025-11-27 14:36:19', '2025-11-27 14:36:19');

-- ----------------------------
-- Table structure for tb_experiment_problem
-- ----------------------------
DROP TABLE IF EXISTS `tb_experiment_problem`;
CREATE TABLE `tb_experiment_problem`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '实验题目ID',
  `experiment_id` bigint NOT NULL COMMENT '关联实验ID',
  `question_id` bigint NULL DEFAULT NULL COMMENT '若源自题库的题目ID(可为空)',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '题目描述(UTF-8)',
  `difficulty` enum('easy','medium','hard') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'easy' COMMENT '难度',
  `score` int NOT NULL DEFAULT 10 COMMENT '题目分值',
  `problem_order` int NOT NULL DEFAULT 1 COMMENT '题目排序',
  `creator_id` bigint NOT NULL COMMENT '创建者ID(教师)',
  `creator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建者姓名(冗余)',
  `max_cpu_time_ms` int NULL DEFAULT 2000 COMMENT '最大运行时间(ms)默认2000',
  `max_memory_kb` int NULL DEFAULT 65536 COMMENT '最大内存(kB)默认65536',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_experiment_id`(`experiment_id` ASC) USING BTREE,
  INDEX `idx_question_id`(`question_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '实验题目表(编程题)' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_experiment_problem
-- ----------------------------
INSERT INTO `tb_experiment_problem` VALUES (1, 6, 1, '法撒旦', '阿斯顿f', 'easy', 10, 1, 1, NULL, 1000, 262144, '2025-11-27 08:21:43', '2025-12-05 13:54:20');
INSERT INTO `tb_experiment_problem` VALUES (2, 7, 1, '1 2', '1 2', 'easy', 10, 1, 1, NULL, 1000, 262144, '2025-11-27 14:36:19', '2025-12-05 13:54:20');

-- ----------------------------
-- Table structure for tb_experiment_report
-- ----------------------------
DROP TABLE IF EXISTS `tb_experiment_report`;
CREATE TABLE `tb_experiment_report`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报告ID',
  `experiment_id` bigint NOT NULL COMMENT '实验ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学生姓名(冗余)',
  `student_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学号(冗余)',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '报告内容',
  `files` json NULL COMMENT '报告文件列表',
  `submit_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `status` enum('submitted','completed') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'submitted' COMMENT '状态',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_experiment_student`(`experiment_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '实验报告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_experiment_report
-- ----------------------------

-- ----------------------------
-- Table structure for tb_file_preview
-- ----------------------------
DROP TABLE IF EXISTS `tb_file_preview`;
CREATE TABLE `tb_file_preview`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预览记录ID',
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `resource_type` enum('resource','homework','experiment','other') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源类型',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件URL',
  `preview_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '预览URL(转换后)',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件类型(pdf/doc/ppt/xls/jpg/mp4等)',
  `file_size` bigint NOT NULL COMMENT '文件大小(字节)',
  `preview_type` enum('online','download','stream') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'online' COMMENT '预览方式',
  `conversion_status` enum('pending','processing','success','failed') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '转换状态',
  `viewer_id` bigint NOT NULL COMMENT '查看者ID',
  `viewer_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '查看者姓名',
  `view_count` int NULL DEFAULT 0 COMMENT '查看次数',
  `last_view_time` datetime NULL DEFAULT NULL COMMENT '最后查看时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_resource_viewer`(`resource_id` ASC, `resource_type` ASC, `viewer_id` ASC) USING BTREE,
  INDEX `idx_resource_id`(`resource_id` ASC) USING BTREE,
  INDEX `idx_viewer_id`(`viewer_id` ASC) USING BTREE,
  INDEX `idx_conversion_status`(`conversion_status` ASC) USING BTREE,
  INDEX `idx_viewer_time`(`viewer_id` ASC, `last_view_time` DESC) USING BTREE,
  INDEX `idx_file_type`(`file_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件预览记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_file_preview
-- ----------------------------
INSERT INTO `tb_file_preview` VALUES (1, 1, 'resource', 'https://oss.example.com/java-basic.pptx', 'https://oss.example.com/preview/java-basic.pdf', 'pptx', 2560000, 'online', 'success', 1, '张三', 15, '2025-11-24 10:30:00', '2025-11-26 22:33:04');
INSERT INTO `tb_file_preview` VALUES (2, 2, 'resource', 'https://oss.example.com/java-oop.pdf', 'https://oss.example.com/java-oop.pdf', 'pdf', 5120000, 'online', 'success', 1, '张三', 8, '2025-11-23 15:20:00', '2025-11-26 22:33:04');
INSERT INTO `tb_file_preview` VALUES (3, 3, 'resource', 'https://oss.example.com/java-intro.mp4', 'https://oss.example.com/java-intro.mp4', 'mp4', 512000000, 'stream', 'success', 2, '李四', 3, '2025-11-22 20:15:00', '2025-11-26 22:33:04');

-- ----------------------------
-- Table structure for tb_homework
-- ----------------------------
DROP TABLE IF EXISTS `tb_homework`;
CREATE TABLE `tb_homework`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '作业ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名称(冗余)',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '作业标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '作业描述说明',
  `total_score` int NOT NULL DEFAULT 100 COMMENT '作业总分',
  `deadline` datetime NOT NULL COMMENT '截止时间',
  `allow_late_submission` tinyint(1) NULL DEFAULT 0 COMMENT '是否允许迟交',
  `late_penalty` int NULL DEFAULT 0 COMMENT '迟交扣分百分比(0-100)',
  `show_answer` tinyint(1) NULL DEFAULT 0 COMMENT '截止后是否显示答案',
  `creator_id` bigint NOT NULL COMMENT '创建者ID(教师)',
  `creator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创建者姓名(冗余)',
  `status` enum('draft','published','closed','archived') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'draft' COMMENT '作业状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_creator_id`(`creator_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_deadline`(`deadline` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作业表-支持题库选题' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_homework
-- ----------------------------
INSERT INTO `tb_homework` VALUES (12, 1, 'Java程序设计', 'Java程序设计', 'java', 70, '2025-12-31 16:00:00', 0, 0, 0, 3, '张教授', 'published', '2025-12-05 01:39:03', '2025-12-05 01:39:06');
INSERT INTO `tb_homework` VALUES (13, 3, '操作系统', '操作系统', '操作系统', 35, '2025-12-31 16:00:00', 0, 0, 0, 3, '张教授', 'published', '2025-12-05 01:39:41', '2025-12-05 01:39:43');

-- ----------------------------
-- Table structure for tb_homework_question
-- ----------------------------
DROP TABLE IF EXISTS `tb_homework_question`;
CREATE TABLE `tb_homework_question`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `homework_id` bigint NOT NULL COMMENT '作业ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `question_order` int NOT NULL COMMENT '题目顺序',
  `question_score` int NOT NULL COMMENT '该题分值',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_homework_question`(`homework_id` ASC, `question_id` ASC) USING BTREE,
  INDEX `idx_homework_id`(`homework_id` ASC) USING BTREE,
  INDEX `idx_question_id`(`question_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 56 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '作业题目关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_homework_question
-- ----------------------------
INSERT INTO `tb_homework_question` VALUES (43, 12, 46, 1, 10);
INSERT INTO `tb_homework_question` VALUES (44, 12, 32, 2, 5);
INSERT INTO `tb_homework_question` VALUES (45, 12, 33, 3, 5);
INSERT INTO `tb_homework_question` VALUES (46, 12, 58, 4, 10);
INSERT INTO `tb_homework_question` VALUES (47, 12, 59, 5, 10);
INSERT INTO `tb_homework_question` VALUES (48, 12, 23, 6, 10);
INSERT INTO `tb_homework_question` VALUES (49, 12, 25, 7, 10);
INSERT INTO `tb_homework_question` VALUES (50, 12, 26, 8, 10);
INSERT INTO `tb_homework_question` VALUES (51, 13, 139, 1, 5);
INSERT INTO `tb_homework_question` VALUES (52, 13, 140, 2, 5);
INSERT INTO `tb_homework_question` VALUES (53, 13, 148, 3, 10);
INSERT INTO `tb_homework_question` VALUES (54, 13, 149, 4, 10);
INSERT INTO `tb_homework_question` VALUES (55, 13, 133, 5, 5);

-- ----------------------------
-- Table structure for tb_learning_progress
-- ----------------------------
DROP TABLE IF EXISTS `tb_learning_progress`;
CREATE TABLE `tb_learning_progress`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '进度ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `resource_id` bigint NULL DEFAULT NULL COMMENT '资源ID',
  `resource_type` enum('video','document','code','other') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源类型',
  `progress_percent` int NULL DEFAULT 0 COMMENT '完成百分比',
  `duration_seconds` int NULL DEFAULT 0 COMMENT '学习时长(秒)',
  `last_position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后位置(视频进度/文档页码)',
  `is_completed` tinyint(1) NULL DEFAULT 0 COMMENT '是否完成',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `last_study_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后学习时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_resource`(`student_id` ASC, `resource_id` ASC) USING BTREE,
  INDEX `idx_student_course`(`student_id` ASC, `course_id` ASC) USING BTREE,
  INDEX `idx_resource_id`(`resource_id` ASC) USING BTREE,
  INDEX `idx_student_complete`(`student_id` ASC, `is_completed` ASC) USING BTREE,
  INDEX `idx_last_study_time`(`last_study_time` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学习进度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_learning_progress
-- ----------------------------
INSERT INTO `tb_learning_progress` VALUES (1, 1, 1, 1, 'document', 100, 1800, '45', 1, '2025-11-20 16:30:00', '2025-11-26 22:33:04');
INSERT INTO `tb_learning_progress` VALUES (2, 1, 1, 2, 'document', 75, 2400, '68', 0, NULL, '2025-11-26 22:33:04');
INSERT INTO `tb_learning_progress` VALUES (3, 1, 1, 3, 'video', 60, 3600, '00:35:20', 0, NULL, '2025-11-26 22:33:04');
INSERT INTO `tb_learning_progress` VALUES (4, 2, 1, 1, 'document', 100, 1500, '45', 1, '2025-11-21 14:20:00', '2025-11-26 22:33:04');
INSERT INTO `tb_learning_progress` VALUES (5, 2, 1, 3, 'video', 30, 1800, '00:18:45', 0, NULL, '2025-11-26 22:33:04');

-- ----------------------------
-- Table structure for tb_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `tb_operation_log`;
CREATE TABLE `tb_operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `operation` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求方法',
  `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP归属地',
  `status` enum('success','failed') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'success' COMMENT '操作状态',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `execution_time` int NULL DEFAULT NULL COMMENT '执行时长(毫秒)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_operation`(`operation` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` DESC) USING BTREE,
  INDEX `idx_user_operation`(`user_id` ASC, `operation` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_ip`(`ip` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_operation_log
-- ----------------------------
INSERT INTO `tb_operation_log` VALUES (1, 1, 'student001', '用户登录', 'POST /auth/login', '{\"username\":\"student001\"}', '192.168.1.100', NULL, 'success', NULL, 125, '2025-11-26 22:33:04');
INSERT INTO `tb_operation_log` VALUES (2, 1, 'student001', '查看课程列表', 'GET /course/list', '{\"page\":1,\"size\":10}', '192.168.1.100', NULL, 'success', NULL, 45, '2025-11-26 22:33:04');
INSERT INTO `tb_operation_log` VALUES (3, 1, 'student001', '选课', 'POST /course/enroll', '{\"courseId\":1}', '192.168.1.100', NULL, 'success', NULL, 230, '2025-11-26 22:33:04');

-- ----------------------------
-- Table structure for tb_problem_sample
-- ----------------------------
DROP TABLE IF EXISTS `tb_problem_sample`;
CREATE TABLE `tb_problem_sample`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '样例点ID',
  `experiment_problem_id` bigint NOT NULL COMMENT '关联实验题目ID',
  `sample_order` int NOT NULL DEFAULT 1 COMMENT '样例序号',
  `input_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '输入样例(原样传给判题API)',
  `output_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '期望输出(用于比较)',
  `is_example` tinyint(1) NULL DEFAULT 1 COMMENT '是否为示例(展示给学生)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_experiment_problem_id`(`experiment_problem_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题目样例点表(输入/输出对)' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_problem_sample
-- ----------------------------
INSERT INTO `tb_problem_sample` VALUES (1, 1, 1, '1 2', '3', 1, '2025-11-27 08:21:43');
INSERT INTO `tb_problem_sample` VALUES (2, 2, 1, '1 2', '3', 1, '2025-11-27 14:36:19');

-- ----------------------------
-- Table structure for tb_question_bank
-- ----------------------------
DROP TABLE IF EXISTS `tb_question_bank`;
CREATE TABLE `tb_question_bank`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目ID',
  `course_id` bigint NULL DEFAULT NULL COMMENT '关联课程ID(可为空表示通用题库)',
  `question_type` enum('single_choice','multiple_choice','true_false','fill_blank','short_answer','programming') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目类型',
  `difficulty` enum('easy','medium','hard') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'medium' COMMENT '难度',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目内容',
  `options` json NULL COMMENT '选项(JSON数组,仅选择题使用)',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '正确答案',
  `explanation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '答案解析',
  `score` int NOT NULL DEFAULT 5 COMMENT '默认分值',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签(逗号分隔)',
  `knowledge_points` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '知识点',
  `usage_count` int NULL DEFAULT 0 COMMENT '使用次数',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `creator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '创建者姓名(冗余)',
  `status` enum('active','inactive') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active' COMMENT '状态',
  `is_visible` int NOT NULL DEFAULT 1 COMMENT '学生是否可见(1=可见,0=不可见,教师全部可见)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_type_difficulty`(`question_type` ASC, `difficulty` ASC) USING BTREE,
  INDEX `idx_creator_id`(`creator_id` ASC) USING BTREE,
  INDEX `idx_course_type`(`course_id` ASC, `question_type` ASC) USING BTREE,
  INDEX `idx_difficulty`(`difficulty` ASC) USING BTREE,
  INDEX `idx_usage_count`(`usage_count` DESC) USING BTREE,
  INDEX `idx_is_visible`(`is_visible` ASC) USING BTREE,
  FULLTEXT INDEX `idx_fulltext_content`(`content`)
) ENGINE = InnoDB AUTO_INCREMENT = 301 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '题库表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_question_bank
-- ----------------------------
INSERT INTO `tb_question_bank` VALUES (1, 1, 'single_choice', 'easy', 'Java 中用于输出信息到控制台的语句是：', '[\"Console.print()\", \"System.out.print()\", \"Output.println()\", \"Print.write()\"]', 'B', 'System.out.print()是Java标准输出语句，用于向控制台输出信息', 5, 'Java基础', 'Java输出', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (2, 1, 'single_choice', 'easy', 'Java 中非基本数据类型的是：', '[\"int\", \"float\", \"double\", \"String\"]', 'D', 'String是引用类型，不是基本数据类型。Java有8种基本数据类型：byte、short、int、long、float、double、char、boolean', 5, 'Java基础', '数据类型', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (3, 1, 'single_choice', 'easy', 'Java 源文件编译后生成的文件扩展名是：', '[\".exe\", \".java\", \".class\", \".jar\"]', 'C', 'Java源文件(.java)编译后生成字节码文件(.class)', 5, 'Java基础', 'Java编译', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (4, 1, 'single_choice', 'easy', '下列哪个关键字用于创建对象：', '[\"class\", \"this\", \"new\", \"make\"]', 'C', 'new关键字用于创建对象实例', 5, 'Java基础', '对象创建', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (5, 1, 'single_choice', 'easy', 'Java 使用哪个关键字来继承父类？', '[\"inherit\", \"extends\", \"implements\", \"override\"]', 'B', 'extends关键字用于类的继承，implements用于接口的实现', 5, 'Java,继承', '继承机制', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (6, 1, 'single_choice', 'easy', 'Java 中用于表示单个字符的类型是：', '[\"char\", \"string\", \"Character\", \"byte\"]', 'A', 'char是Java中表示单个字符的基本数据类型', 5, 'Java基础', '字符类型', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (7, 1, 'single_choice', 'easy', '下列关于构造方法的说法正确的是：', '[\"名称可以任意\", \"必须与类名相同\", \"必须有返回类型\", \"必须使用 static\"]', 'B', '构造方法名必须与类名完全相同，且没有返回类型', 5, 'Java基础', '构造方法', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (8, 1, 'single_choice', 'easy', '下列哪个是 Java 的逻辑运算符：', '[\"++\", \"&&\", \"==\", \"::\"]', 'B', '&&是逻辑与运算符，++是自增运算符，==是关系运算符，::是方法引用运算符', 5, 'Java基础', '运算符', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (9, 1, 'single_choice', 'easy', '下列哪种访问修饰符权限最大：', '[\"private\", \"protected\", \"public\", \"default\"]', 'C', 'public访问权限最大，可以被任何类访问。权限从大到小：public > protected > default > private', 5, 'Java基础', '访问修饰符', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (10, 1, 'single_choice', 'medium', '下列哪个集合是线程安全的？', '[\"ArrayList\", \"HashMap\", \"Vector\", \"HashSet\"]', 'C', 'Vector是线程安全的集合类，内部方法使用synchronized关键字同步', 5, 'Java,集合', '线程安全', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (11, 1, 'single_choice', 'easy', '接口使用哪个关键字来实现？', '[\"extends\", \"include\", \"import\", \"implements\"]', 'D', 'implements关键字用于类实现接口', 5, 'Java,接口', '接口实现', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (12, 1, 'single_choice', 'medium', '下列关于异常处理的描述正确的是：', '[\"try 后必须有 finally\", \"catch 可以没有\", \"try 后必须至少一个 catch 或 finally\", \"try 可以单独存在\"]', 'C', 'try语句后面必须跟至少一个catch块或一个finally块，或者两者都有', 5, 'Java,异常', '异常处理', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (13, 1, 'single_choice', 'medium', '下列哪个属于受检异常（Checked Exception）？', '[\"NullPointerException\", \"ArithmeticException\", \"IOException\", \"ArrayIndexOutOfBoundsException\"]', 'C', 'IOException是受检异常，必须显式捕获或声明抛出。其他三个都是运行时异常(RuntimeException)', 5, 'Java,异常', '异常类型', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (14, 1, 'single_choice', 'medium', 'String 属于哪种类型？', '[\"可变对象\", \"不可变对象\", \"基本类型\", \"指针类型\"]', 'B', 'String是不可变对象(immutable)，每次修改都会生成新对象。要使用可变字符串应使用StringBuilder或StringBuffer', 5, 'String', 'String类', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (15, 1, 'single_choice', 'medium', '下列对多态的描述正确的是：', '[\"子类不能重写父类方法\", \"多态发生于编译期\", \"同一方法表现出不同形态\", \"多态与继承无关\"]', 'C', '多态是指同一方法在不同对象中表现出不同的行为，包括编译时多态(方法重载)和运行时多态(方法重写)', 5, 'Java,多态', '多态特性', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (16, 1, 'single_choice', 'easy', '哪个包中包含集合框架？', '[\"java.lang\", \"java.io\", \"java.util\", \"java.net\"]', 'C', 'java.util包包含了Java集合框架的所有接口和类', 5, 'Java,集合', '集合框架', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (17, 1, 'single_choice', 'medium', '下列关于 static 的描述正确的是：', '[\"static 成员属于对象\", \"static 方法能直接访问实例变量\", \"static 成员属于类\", \"static 只能用于方法\"]', 'C', 'static成员属于类而不是对象，类的所有实例共享static成员。static方法不能直接访问非static成员', 5, 'Java,static', 'static关键字', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (18, 1, 'single_choice', 'medium', '下列哪种线程创建方式正确？', '[\"new Thread().run()\", \"实现 Runnable 接口\", \"重写 Object 类\", \"使用 class ThreadStart\"]', 'B', 'Java创建线程的方式：1.继承Thread类 2.实现Runnable接口 3.实现Callable接口。推荐使用接口方式', 5, 'Java,多线程', '线程创建', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (19, 1, 'single_choice', 'medium', '下列哪种文件流用于读取文本文件？', '[\"FileReader\", \"FileOutputStream\", \"ByteArrayOutputStream\", \"DataInputStream\"]', 'A', 'FileReader用于读取字符文件，FileInputStream用于读取字节文件', 5, 'Java,IO', '文件流', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (20, 1, 'single_choice', 'medium', 'Java 中下列哪个是保留字？', '[\"goto\", \"constant\", \"main\", \"sizeof\"]', 'A', 'goto是Java的保留字，虽然不使用但仍是关键字', 5, 'Java基础', 'Java关键字', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (21, 1, 'multiple_choice', 'easy', 'Java 的基本数据类型包括：', '[\"int\", \"boolean\", \"String\", \"double\"]', 'A,B,D', 'Java的8种基本数据类型：byte、short、int、long、float、double、char、boolean。String是引用类型', 10, 'Java基础', '基本数据类型', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (22, 1, 'multiple_choice', 'easy', 'Java 中面向对象的特性包括：', '[\"封装\", \"继承\", \"多态\", \"编译\"]', 'A,B,C', '面向对象三大特性：封装、继承、多态。编译不是面向对象的特性', 10, 'Java,面向对象', '面向对象特性', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (23, 1, 'multiple_choice', 'medium', '下列属于 Java 集合框架中的接口：', '[\"List\", \"HashMap\", \"Set\", \"Queue\"]', 'A,C,D', 'List、Set、Queue都是集合框架的接口。HashMap是Map接口的实现类', 10, 'Java,集合', '集合接口', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (24, 1, 'multiple_choice', 'medium', '下列哪些类属于线程安全：', '[\"Vector\", \"Hashtable\", \"ArrayList\", \"StringBuffer\"]', 'A,B,D', 'Vector、Hashtable、StringBuffer都是线程安全的类。ArrayList、HashMap、StringBuilder不是线程安全的', 10, 'Java,多线程', '线程安全', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (25, 1, 'multiple_choice', 'medium', '下列哪些属于异常类型：', '[\"Throwable\", \"Error\", \"Exception\", \"RuntimeException\"]', 'A,B,C,D', 'Throwable是所有异常和错误的超类，Error和Exception是其两个子类，RuntimeException是Exception的子类', 10, 'Java,异常', '异常体系', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (26, 1, 'multiple_choice', 'medium', '下列哪些语句可以终止循环：', '[\"continue\", \"break\", \"return\", \"exit\"]', 'B,C', 'break用于退出循环，return用于退出方法(也会终止循环)。continue是跳过当前循环继续下一次，exit不是Java关键字', 10, 'Java基础', '循环控制', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (27, 1, 'multiple_choice', 'medium', '下列哪些类可用于文件 I/O 操作：', '[\"FileInputStream\", \"FileWriter\", \"Scanner\", \"URL\"]', 'A,B,C', 'FileInputStream、FileWriter、Scanner都可用于文件I/O操作。URL用于网络资源定位', 10, 'Java,IO', '文件IO', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (28, 1, 'multiple_choice', 'medium', '下列哪些为 Java 关键字：', '[\"this\", \"null\", \"instanceof\", \"define\"]', 'A,C', 'this和instanceof是Java关键字。null是字面量不是关键字，define不是Java关键字', 10, 'Java基础', 'Java关键字', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (29, 1, 'multiple_choice', 'medium', 'Java 中可实现多线程的方式有：', '[\"继承 Thread 类\", \"实现 Runnable 接口\", \"实现 AutoCloseable\", \"使用线程池\"]', 'A,B,D', 'Java实现多线程的方式：1.继承Thread类 2.实现Runnable接口 3.实现Callable接口 4.使用线程池。AutoCloseable用于资源管理', 10, 'Java,多线程', '多线程实现', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (30, 1, 'multiple_choice', 'medium', '下列哪些属于 HashMap 的特性：', '[\"键不可重复\", \"值不可重复\", \"底层使用哈希表\", \"允许 null 键\"]', 'A,C,D', 'HashMap的特性：键不可重复、值可以重复、底层使用哈希表实现、允许一个null键和多个null值', 10, 'Java,集合', 'HashMap特性', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (31, 1, 'true_false', 'easy', 'Java 是跨平台语言。', NULL, 'true', 'Java通过JVM实现跨平台，一次编译到处运行(Write Once, Run Anywhere)', 5, 'Java基础', 'Java特性', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (32, 1, 'true_false', 'easy', '一个 Java 文件可以包含多个 public 类。', NULL, 'false', '一个Java源文件(.java)只能有一个public类，且类名必须与文件名相同', 5, 'Java基础', 'Java类', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (33, 1, 'true_false', 'medium', '基本类型作为方法参数是按值传递的。', NULL, 'true', 'Java中基本类型是按值传递，引用类型传递的是引用的副本', 5, 'Java基础', '参数传递', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (34, 1, 'true_false', 'medium', 'finally 块一定会执行。', NULL, 'false', 'finally块通常会执行，但System.exit()、JVM崩溃等情况下不会执行', 5, 'Java,异常', 'finally块', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (35, 1, 'true_false', 'medium', 'abstract 类可以有构造方法。', NULL, 'true', '抽象类可以有构造方法，用于子类调用初始化父类成员', 5, 'Java,抽象类', '抽象类', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (36, 1, 'true_false', 'easy', '接口中的方法默认是 public。', NULL, 'true', '接口中的方法默认是public abstract的，即使不写也会自动添加', 5, 'Java,接口', '接口特性', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (37, 1, 'true_false', 'medium', '枚举类型可以定义方法。', NULL, 'true', '枚举类型可以定义字段、构造方法和普通方法', 5, 'Java,枚举', '枚举类型', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (38, 1, 'true_false', 'medium', 'StringBuilder 是线程安全的。', NULL, 'false', 'StringBuilder不是线程安全的，StringBuffer是线程安全的', 5, 'Java,String', 'StringBuilder', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (39, 1, 'true_false', 'easy', 'Java 中数组下标从 1 开始。', NULL, 'false', 'Java数组下标从0开始', 5, 'Java基础', '数组', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (40, 1, 'true_false', 'easy', '继承可以实现代码复用。', NULL, 'true', '继承是代码复用的重要手段，子类可以继承父类的属性和方法', 5, 'Java,继承', '继承特性', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (41, 1, 'true_false', 'medium', '构造方法可以被重载。', NULL, 'true', '构造方法可以重载，一个类可以有多个不同参数的构造方法', 5, 'Java基础', '构造方法', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (42, 1, 'true_false', 'medium', 'try...catch 可以捕获所有异常。', NULL, 'false', 'Error类异常通常不应被捕获，它们表示严重的系统错误', 5, 'Java,异常', '异常捕获', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (43, 1, 'true_false', 'medium', 'super() 必须是构造方法中的第一句。', NULL, 'true', 'super()或this()必须是构造方法的第一条语句', 5, 'Java基础', 'super关键字', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (44, 1, 'true_false', 'medium', 'final 修饰的变量必须在声明时初始化。', NULL, 'false', 'final变量可以在声明时初始化，也可以在构造方法中初始化(空白final)', 5, 'Java,final', 'final关键字', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (45, 1, 'true_false', 'medium', '垃圾回收器可以释放任何资源。', NULL, 'false', '垃圾回收器只能释放内存资源，不能释放文件句柄、数据库连接等其他资源', 5, 'Java,GC', '垃圾回收', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (46, 1, 'fill_blank', 'easy', 'Java 中定义类使用的关键字是______。', NULL, 'class', 'class关键字用于定义类', 10, 'Java基础', 'Java类', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (47, 1, 'fill_blank', 'easy', 'Java 中用于创建对象的关键字是______。', NULL, 'new', 'new关键字用于创建对象实例', 10, 'Java基础', '对象创建', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (48, 1, 'fill_blank', 'medium', '使用______关键字表示类不可被继承。', NULL, 'final', 'final关键字修饰的类不能被继承', 10, 'Java,继承', 'final关键字', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (49, 1, 'fill_blank', 'medium', '子类调用父类构造方法的语句是______。', NULL, 'super()', 'super()用于调用父类构造方法，必须是构造方法的第一条语句', 10, 'Java,继承', 'super关键字', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (50, 1, 'fill_blank', 'easy', 'Java 中用于包导入的关键字是______。', NULL, 'import', 'import关键字用于导入其他包中的类', 10, 'Java基础', 'import关键字', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (51, 1, 'fill_blank', 'easy', 'Java 中字符串连接常用运算符是______。', NULL, '+', '+运算符可以用于字符串连接', 10, 'Java,String', '字符串操作', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (52, 1, 'fill_blank', 'medium', '判断两个字符串内容是否相等使用方法______。', NULL, 'equals()', 'equals()方法用于比较字符串内容，==比较的是引用地址', 10, 'Java,String', '字符串比较', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (53, 1, 'fill_blank', 'medium', '异常捕获使用的关键字是______。', NULL, 'try;catch', 'try用于包含可能抛出异常的代码，catch用于捕获异常', 10, 'Java,异常', '异常处理', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (54, 1, 'fill_blank', 'medium', 'Java 中线程休眠使用的方法是______。', NULL, 'Thread.sleep()', 'Thread.sleep()方法使当前线程休眠指定的毫秒数', 10, 'Java,多线程', '线程休眠', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (55, 1, 'fill_blank', 'medium', '表示无限循环的语句为______。', NULL, 'while(true)', 'while(true)或for(;;)都可以表示无限循环', 10, 'Java基础', '循环语句', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (56, 1, 'fill_blank', 'medium', '读取控制台输入常用的类是______。', NULL, 'Scanner', 'Scanner类用于读取控制台输入，位于java.util包中', 10, 'Java,IO', '输入输出', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (57, 1, 'fill_blank', 'medium', 'Java 中可变长参数使用符号______。', NULL, '...', '...表示可变长参数，如void method(String... args)', 10, 'Java基础', '可变参数', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (58, 1, 'fill_blank', 'medium', 'Java 中 List 的常用实现类有 ArrayList 和______。', NULL, 'LinkedList', 'List接口常用实现类有ArrayList和LinkedList', 10, 'Java,集合', 'List接口', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (59, 1, 'fill_blank', 'medium', 'Java 中 HashMap 是基于______实现的。', NULL, '哈希表', 'HashMap基于哈希表(散列表)实现，提供O(1)的查找效率', 10, 'Java,集合', 'HashMap实现', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (60, 1, 'fill_blank', 'medium', 'Java 中 main 方法必须声明为______。', NULL, 'public static void main(String[] args)', 'main方法是程序入口，必须是public static void，参数是String数组', 10, 'Java基础', 'main方法', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (61, 2, 'single_choice', 'easy', 'HTML 中用于定义超链接的标签是：', '[\"<link>\", \"<a>\", \"<href>\", \"<url>\"]', 'B', '<a>标签用于定义超链接，href属性指定链接目标地址', 5, 'HTML基础', '超链接', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (62, 2, 'single_choice', 'easy', 'CSS 用于设置文本颜色的属性是：', '[\"background-color\", \"color\", \"font-color\", \"text-color\"]', 'B', 'color属性用于设置文本颜色，background-color用于设置背景颜色', 5, 'CSS基础', 'CSS属性', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (63, 2, 'single_choice', 'medium', 'HTML5 中用于绘图的标签是：', '[\"<svg>\", \"<canvas>\", \"<draw>\", \"<paint>\"]', 'B', '<canvas>标签提供了通过JavaScript绘制图形的API，<svg>是矢量图形标签', 5, 'HTML5', 'Canvas', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (64, 2, 'single_choice', 'easy', 'JavaScript 中用于声明常量的关键字是：', '[\"const\", \"var\", \"let\", \"constant\"]', 'A', 'const用于声明常量，let用于声明变量，var是旧的变量声明方式', 5, 'JavaScript', '变量声明', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (65, 2, 'single_choice', 'easy', 'HTTP 的默认端口号是：', '[\"443\", \"22\", \"80\", \"21\"]', 'C', 'HTTP默认端口80，HTTPS默认端口443，SSH端口22，FTP端口21', 5, 'HTTP,网络', 'HTTP协议', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (66, 2, 'single_choice', 'easy', '下列哪个不是 HTTP 请求方法：', '[\"GET\", \"POST\", \"DELETE\", \"FETCH\"]', 'D', 'HTTP请求方法包括GET、POST、PUT、DELETE、PATCH等，FETCH是JavaScript中的API而非HTTP方法', 5, 'HTTP', 'HTTP方法', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (67, 2, 'single_choice', 'easy', 'CSS 中用于设置元素外边距的属性是：', '[\"padding\", \"border\", \"margin\", \"space\"]', 'C', 'margin设置外边距，padding设置内边距，border设置边框', 5, 'CSS基础', 'CSS盒模型', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (68, 2, 'single_choice', 'easy', 'HTML 页面默认编码推荐使用：', '[\"ASCII\", \"UTF-8\", \"GB2312\", \"ISO-8859-1\"]', 'B', 'UTF-8是Unicode的实现方式，支持全球所有语言字符，是Web开发的推荐编码', 5, 'HTML基础', '字符编码', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (69, 2, 'single_choice', 'medium', 'JavaScript 中 == 和 === 的区别是：', '[\"功能相同\", \"=== 不进行隐式类型转换\", \"== 更严格\", \"=== 只能用于数字\"]', 'B', '==会进行隐式类型转换后比较，===是严格相等，不进行类型转换', 5, 'JavaScript', '相等比较', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (70, 2, 'single_choice', 'easy', '在 HTML 中，表格行使用的标签是：', '[\"<tr>\", \"<td>\", \"<th>\", \"<row>\"]', 'A', '<tr>表示表格行(table row)，<td>表示单元格(table data)，<th>表示表头单元格', 5, 'HTML基础', '表格标签', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (71, 2, 'single_choice', 'easy', 'CSS 中用于选择所有标签的选择器是：', '[\".\", \"#\", \"*\", \"%\"]', 'C', '*是通配符选择器，选择所有元素。.用于类选择器，#用于ID选择器', 5, 'CSS基础', 'CSS选择器', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (72, 2, 'single_choice', 'easy', 'JavaScript 中可以输出到浏览器控制台的方法是：', '[\"print()\", \"console.log()\", \"alert()\", \"show()\"]', 'B', 'console.log()用于输出到浏览器控制台，alert()弹出警告框，print()用于打印页面', 5, 'JavaScript', '输出方法', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (73, 2, 'single_choice', 'easy', '用于提交表单数据到服务器的标签是：', '[\"<info>\", \"<send>\", \"<form>\", \"<table>\"]', 'C', '<form>标签用于创建HTML表单，提交数据到服务器', 5, 'HTML基础', '表单', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (74, 2, 'single_choice', 'easy', 'CSS 中设置字体大小的属性是：', '[\"font-size\", \"text-size\", \"font-style\", \"size\"]', 'A', 'font-size用于设置字体大小，font-style用于设置字体样式(如斜体)', 5, 'CSS基础', 'CSS字体', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (75, 2, 'single_choice', 'easy', 'HTML 中注释的写法是：', '[\"// comment\", \"/* comment */\", \"<!-- comment -->\", \"# comment\"]', 'C', 'HTML注释格式是<!-- 注释内容 -->，//是JavaScript单行注释，/* */是CSS/JS多行注释', 5, 'HTML基础', 'HTML注释', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (76, 2, 'single_choice', 'easy', 'JavaScript 获取元素使用的方法是：', '[\"document.find()\", \"document.getElementById()\", \"document.search()\", \"document.query()\"]', 'B', 'document.getElementById()通过ID获取元素，document.querySelector()可以通过CSS选择器获取元素', 5, 'JavaScript,DOM', 'DOM操作', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (77, 2, 'single_choice', 'easy', 'CSS 中设置背景颜色使用：', '[\"text-bg\", \"background\", \"background-color\", \"color-bg\"]', 'C', 'background-color专门用于设置背景颜色，background是复合属性可设置多个背景相关属性', 5, 'CSS基础', 'CSS背景', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (78, 2, 'single_choice', 'easy', '哪种格式常用于网页中存放样式文件？', '[\".css\", \".java\", \".exe\", \".web\"]', 'A', '.css是层叠样式表(Cascading Style Sheets)文件的扩展名', 5, 'CSS基础', 'CSS文件', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (79, 2, 'single_choice', 'medium', 'HTML DOM 是：', '[\"数据库模型\", \"文档对象模型\", \"网络拓扑结构\", \"编码方式\"]', 'B', 'DOM(Document Object Model)是文档对象模型，是HTML和XML文档的编程接口', 5, 'DOM', 'DOM概念', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (80, 2, 'single_choice', 'easy', '浏览器负责解释执行的脚本语言是：', '[\"Python\", \"JavaScript\", \"PHP\", \"Java\"]', 'B', 'JavaScript是浏览器端脚本语言，PHP在服务器端执行', 5, 'JavaScript', 'JavaScript基础', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (81, 2, 'multiple_choice', 'medium', '下列哪些属于 HTML5 新特性：', '[\"video 标签\", \"audio 标签\", \"canvas 标签\", \"table 标签\"]', 'A,B,C', 'HTML5新增了video、audio、canvas等标签，table是早期HTML就有的标签', 10, 'HTML5', 'HTML5特性', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (82, 2, 'multiple_choice', 'medium', '下列属于 HTTP 请求方法的是：', '[\"GET\", \"PUT\", \"SEND\", \"PATCH\"]', 'A,B,D', 'HTTP标准请求方法包括GET、POST、PUT、DELETE、PATCH等，SEND不是HTTP方法', 10, 'HTTP', 'HTTP方法', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (83, 2, 'multiple_choice', 'easy', '下列属于常见 CSS 选择器的是：', '[\"id 选择器\", \"类选择器\", \"标签选择器\", \"目录选择器\"]', 'A,B,C', 'CSS选择器包括：id选择器(#)、类选择器(.)、标签选择器、属性选择器等，没有目录选择器', 10, 'CSS', 'CSS选择器', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (84, 2, 'multiple_choice', 'medium', 'JavaScript 的数据类型包括：', '[\"string\", \"boolean\", \"number\", \"map\"]', 'A,B,C', 'JavaScript基本数据类型包括：string、number、boolean、null、undefined、symbol、bigint。Map是引用类型', 10, 'JavaScript', '数据类型', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (85, 2, 'multiple_choice', 'medium', '下列哪些方式可以触发 JavaScript 事件：', '[\"onclick\", \"onkeyup\", \"onload\", \"onlisten\"]', 'A,B,C', 'JavaScript常见事件包括：onclick(点击)、onkeyup(按键抬起)、onload(加载完成)等，没有onlisten事件', 10, 'JavaScript', 'JavaScript事件', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (86, 2, 'multiple_choice', 'easy', '下列哪些语句可用于控制流程：', '[\"if\", \"switch\", \"while\", \"do…while\"]', 'A,B,C,D', 'JavaScript流程控制语句包括：if、switch、while、do-while、for等', 10, 'JavaScript', '流程控制', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (87, 2, 'multiple_choice', 'medium', 'CSS 哪些属性可用于设置内边距：', '[\"padding-top\", \"padding-left\", \"padding\", \"inner-space\"]', 'A,B,C', 'padding相关属性：padding-top、padding-right、padding-bottom、padding-left、padding(复合属性)', 10, 'CSS', 'CSS盒模型', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (88, 2, 'multiple_choice', 'medium', '下列哪些用于存储浏览器端数据：', '[\"localStorage\", \"sessionStorage\", \"cookie\", \"SQLStorage\"]', 'A,B,C', '浏览器存储方式：localStorage(永久存储)、sessionStorage(会话存储)、cookie(小型文本文件)。没有SQLStorage', 10, 'JavaScript', '浏览器存储', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (89, 2, 'multiple_choice', 'medium', '下列哪些属于前端框架：', '[\"Vue\", \"React\", \"Spring\", \"Angular\"]', 'A,B,D', '前端框架包括Vue、React、Angular等，Spring是Java后端框架', 10, '前端框架', '前端技术栈', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (90, 2, 'multiple_choice', 'medium', 'AJAX 技术包含：', '[\"JavaScript\", \"XML/JSON\", \"XMLHttpRequest\", \"PHP\"]', 'A,B,C', 'AJAX(Asynchronous JavaScript and XML)包含：JavaScript、XML/JSON、XMLHttpRequest对象。PHP是服务器端语言', 10, 'AJAX', 'AJAX技术', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (91, 2, 'true_false', 'easy', 'HTML 是一种标记语言，不是编程语言。', NULL, 'true', 'HTML(HyperText Markup Language)是标记语言，用于描述网页结构，不具备编程语言的逻辑处理能力', 5, 'HTML基础', 'HTML概念', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (92, 2, 'true_false', 'easy', 'CSS 可以设置 HTML 元素的样式。', NULL, 'true', 'CSS(Cascading Style Sheets)层叠样式表专门用于设置HTML元素的样式', 5, 'CSS基础', 'CSS概念', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (93, 2, 'true_false', 'easy', 'JavaScript 不能操作 DOM。', NULL, 'false', 'JavaScript可以通过DOM API操作HTML文档，这是JavaScript的核心功能之一', 5, 'JavaScript,DOM', 'DOM操作', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (94, 2, 'true_false', 'easy', 'GET 请求的数据不会出现在 URL 中。', NULL, 'false', 'GET请求的参数会以查询字符串的形式附加在URL后面，POST请求的数据在请求体中', 5, 'HTTP', 'HTTP方法', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (95, 2, 'true_false', 'easy', 'CSS 中 class 选择器以 \".\" 开头。', NULL, 'true', 'CSS中类选择器使用.开头，如.myClass；ID选择器使用#开头，如#myId', 5, 'CSS', 'CSS选择器', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (96, 2, 'true_false', 'easy', '内嵌式 CSS 使用 <style> 标签。', NULL, 'true', 'CSS引入方式：内嵌式(<style>标签)、内联式(style属性)、外链式(<link>标签)', 5, 'CSS', 'CSS引入', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (97, 2, 'true_false', 'medium', 'JavaScript 是强类型语言。', NULL, 'false', 'JavaScript是弱类型(动态类型)语言，变量类型在运行时确定，可以改变类型', 5, 'JavaScript', 'JavaScript特性', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (98, 2, 'true_false', 'medium', 'Flex 布局是 CSS3 的新特性。', NULL, 'true', 'Flexbox弹性盒子布局是CSS3引入的新布局模式，提供更灵活的布局方式', 5, 'CSS3', 'Flex布局', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (99, 2, 'true_false', 'easy', 'form 表单提交必须使用 JavaScript。', NULL, 'false', 'form表单可以直接通过HTML提交(action属性)，也可以通过JavaScript提交', 5, 'HTML', '表单提交', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (100, 2, 'true_false', 'medium', 'sessionStorage 在浏览器关闭后仍保留数据。', NULL, 'false', 'sessionStorage数据在会话结束(浏览器关闭)后清除，localStorage数据永久保存', 5, 'JavaScript', '浏览器存储', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (101, 2, 'true_false', 'easy', 'HTML 中标签大小写不敏感。', NULL, 'true', 'HTML标签大小写不敏感，<div>和<DIV>效果相同，但推荐使用小写', 5, 'HTML基础', 'HTML规范', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (102, 2, 'true_false', 'medium', 'JavaScript 的数组长度不可变。', NULL, 'false', 'JavaScript数组长度是动态的，可以通过push、pop等方法改变长度', 5, 'JavaScript', 'JavaScript数组', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (103, 2, 'true_false', 'easy', 'CSS 中 border 可设置边框样式。', NULL, 'true', 'border属性可以设置边框的宽度、样式和颜色', 5, 'CSS', 'CSS边框', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (104, 2, 'true_false', 'medium', 'POST 请求比 GET 更安全。', NULL, 'true', 'POST请求数据在请求体中不显示在URL中，相对GET更安全，但仍需HTTPS等加密措施', 5, 'HTTP', 'HTTP安全', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (105, 2, 'true_false', 'medium', 'CDN 可提高静态资源访问速度。', NULL, 'true', 'CDN(内容分发网络)通过将资源缓存到离用户更近的节点，提高访问速度', 5, 'Web优化', 'CDN', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (106, 2, 'fill_blank', 'easy', 'HTML 用于显示图片的标签是______。', NULL, 'img', '<img>标签用于在网页中嵌入图片，src属性指定图片路径', 10, 'HTML基础', 'HTML标签', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (107, 2, 'fill_blank', 'easy', 'CSS 类选择器以符号______开头。', NULL, '.', '类选择器以.开头，ID选择器以#开头', 10, 'CSS', 'CSS选择器', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (108, 2, 'fill_blank', 'easy', 'JavaScript 中输出警告框的函数是______。', NULL, 'alert()', 'alert()函数弹出警告对话框，用于提示信息', 10, 'JavaScript', 'JavaScript函数', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (109, 2, 'fill_blank', 'easy', 'HTML 文档顶部声明使用的语法是______。', NULL, '<!DOCTYPE html>', '<!DOCTYPE html>是HTML5的文档类型声明，必须位于文档第一行', 10, 'HTML基础', 'HTML结构', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (110, 2, 'fill_blank', 'easy', '将 CSS 文件引入 HTML 使用的标签是______。', NULL, 'link', '<link rel=\"stylesheet\" href=\"style.css\">用于引入外部CSS文件', 10, 'HTML,CSS', 'CSS引入', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (111, 2, 'fill_blank', 'medium', 'JavaScript 中用于获取所有 class 为 box 的元素方法是______。', NULL, 'document.getElementsByClassName(\"box\")', 'getElementsByClassName()返回指定类名的所有元素集合', 10, 'JavaScript,DOM', 'DOM操作', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (112, 2, 'fill_blank', 'medium', 'CSS 中用于设置字体属性的属性组是______。', NULL, 'font', 'font是复合属性，可同时设置font-size、font-family、font-weight等', 10, 'CSS', 'CSS字体', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (113, 2, 'fill_blank', 'medium', 'JavaScript 中转换字符串为整数使用______。', NULL, 'parseInt()', 'parseInt()将字符串解析为整数，parseFloat()解析为浮点数', 10, 'JavaScript', '类型转换', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (114, 2, 'fill_blank', 'medium', '控制元素隐藏使用 CSS 属性______。', NULL, 'display: none', 'display: none完全隐藏元素，visibility: hidden隐藏但保留空间', 10, 'CSS', 'CSS显示', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (115, 2, 'fill_blank', 'easy', 'HTML 注释的格式是______。', NULL, '<!-- -->', 'HTML注释格式：<!-- 注释内容 -->', 10, 'HTML基础', 'HTML注释', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (116, 2, 'fill_blank', 'medium', 'HTTP 中表示服务器成功响应的状态码是______。', NULL, '200', '200 OK表示请求成功，404表示未找到，500表示服务器错误', 10, 'HTTP', 'HTTP状态码', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (117, 2, 'fill_blank', 'easy', 'JavaScript 事件中，鼠标点击事件是______。', NULL, 'onclick', '常见鼠标事件：onclick(点击)、ondblclick(双击)、onmouseover(移入)、onmouseout(移出)', 10, 'JavaScript', 'JavaScript事件', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (118, 2, 'fill_blank', 'easy', 'CSS 设置文本居中的属性是______。', NULL, 'text-align', 'text-align: center设置文本水平居中', 10, 'CSS', 'CSS文本', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (119, 2, 'fill_blank', 'medium', 'JavaScript 中 JSON 格式解析使用______。', NULL, 'JSON.parse()', 'JSON.parse()将JSON字符串转换为对象，JSON.stringify()将对象转换为JSON字符串', 10, 'JavaScript', 'JSON处理', 0, 3, '王讲师', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (120, 2, 'fill_blank', 'medium', 'Ajax 中用于发送请求的对象是______。', NULL, 'XMLHttpRequest', 'XMLHttpRequest对象用于在后台与服务器交换数据，现代开发更常使用fetch API', 10, 'AJAX', 'AJAX对象', 0, 3, '王讲师', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (121, 3, 'single_choice', 'easy', '操作系统的主要功能不包括：', '[\"进程管理\", \"内存管理\", \"文件管理\", \"编译程序\"]', 'D', '操作系统的主要功能包括进程管理、内存管理、文件管理、设备管理等。编译程序是应用软件，不属于操作系统功能', 5, '操作系统', '操作系统基础', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (122, 3, 'single_choice', 'medium', '进程从就绪状态进入运行状态需要：', '[\"I/O 完成\", \"分配 CPU\", \"资源释放\", \"创建新线程\"]', 'B', '进程从就绪状态到运行状态需要获得CPU时间片，由调度器分配CPU', 5, '进程管理', '进程状态转换', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (123, 3, 'single_choice', 'medium', '下面哪个是产生死锁的必要条件之一：', '[\"可剥夺\", \"资源重复\", \"循环等待\", \"快速响应\"]', 'C', '死锁产生的四个必要条件：互斥、不可剥夺、请求与保持、循环等待', 5, '死锁', '死锁条件', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (124, 3, 'single_choice', 'medium', '时间片轮转算法属于：', '[\"非抢占式调度\", \"抢占式调度\", \"顺序调度\", \"优先级调度\"]', 'B', '时间片轮转(RR)是抢占式调度算法，每个进程分配固定时间片，时间到则被抢占', 5, '进程调度', '调度算法', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (125, 3, 'single_choice', 'medium', '下列哪个数据结构最适合实现就绪队列：', '[\"栈\", \"队列\", \"树\", \"栈或队列都行\"]', 'B', '就绪队列采用FIFO(先进先出)方式，队列是最适合的数据结构', 5, '进程管理', '就绪队列', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (126, 3, 'single_choice', 'medium', '页式存储中，程序的逻辑地址由页号和______组成：', '[\"容量\", \"偏移量\", \"物理块号\", \"虚拟号\"]', 'B', '页式存储的逻辑地址由页号和页内偏移量两部分组成', 5, '内存管理', '页式存储', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (127, 3, 'single_choice', 'medium', '下列哪种调度算法可能导致\"饥饿\"现象？', '[\"FCFS\", \"RR\", \"优先级调度\", \"SJF（最短作业优先）\"]', 'C', '优先级调度中，低优先级进程可能一直得不到执行，产生饥饿现象。SJF也可能导致饥饿', 5, '进程调度', '饥饿问题', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (128, 3, 'single_choice', 'medium', '下列哪项属于中断的作用？', '[\"随机访问内存\", \"实现异步处理\", \"提高 CPU 主频\", \"增加磁盘容量\"]', 'B', '中断机制可以实现异步处理，提高CPU利用率，实现多道程序并发执行', 5, '中断', '中断机制', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (129, 3, 'single_choice', 'easy', '操作系统内核运行在：', '[\"用户态\", \"管理态\", \"内核态\", \"应用态\"]', 'C', '操作系统分为内核态(核心态)和用户态，内核运行在内核态，具有最高权限', 5, '操作系统', '内核态', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (130, 3, 'single_choice', 'medium', '实现并发执行的最小单位是：', '[\"程序\", \"线程\", \"进程\", \"指令\"]', 'B', '线程是CPU调度和执行的最小单位，进程是资源分配的基本单位', 5, '线程', '线程概念', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (131, 3, 'single_choice', 'medium', '文件系统中用于记录文件基本信息的是：', '[\"FAT 表\", \"文件夹\", \"i 节点（inode）\", \"主引导记录\"]', 'C', 'inode(索引节点)存储文件的元数据信息，如文件大小、权限、时间戳等', 5, '文件系统', 'inode', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (132, 3, 'single_choice', 'hard', '下列哪种算法可以避免死锁？', '[\"银行家算法\", \"FCFS\", \"LRU\", \"FIFO\"]', 'A', '银行家算法通过安全性检测避免系统进入不安全状态，从而避免死锁', 5, '死锁', '死锁避免', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (133, 3, 'single_choice', 'medium', '发生缺页中断时，操作系统执行：', '[\"终止进程\", \"从磁盘调入页面\", \"清空内存\", \"释放 CPU\"]', 'B', '缺页中断时，操作系统从磁盘调入所需页面到内存，这是虚拟内存的核心机制', 5, '内存管理', '缺页中断', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (134, 3, 'single_choice', 'medium', '在段式管理中，段的大小：', '[\"固定\", \"可变\", \"为 4KB\", \"必须等于页大小\"]', 'B', '段式管理中，段的大小可变，根据程序逻辑结构划分。页式管理的页大小固定', 5, '内存管理', '段式管理', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (135, 3, 'single_choice', 'medium', '在虚拟内存技术中，最常用的置换算法是：', '[\"FIFO\", \"OPT\", \"LRU\", \"LFU\"]', 'C', 'LRU(最近最少使用)算法性能较好，在实际系统中应用广泛。OPT是理论最优但无法实现', 5, '内存管理', '页面置换', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (136, 3, 'single_choice', 'medium', '临界区问题不能依靠下列哪种方式解决：', '[\"互斥锁\", \"信号量\", \"Peterson 算法\", \"调度器完全禁止并发\"]', 'D', '临界区需要通过同步机制保证互斥访问，而不是禁止并发。禁止并发会严重降低系统性能', 5, '进程同步', '临界区', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (137, 3, 'single_choice', 'medium', '下列哪一个是线程间同步机制：', '[\"互斥锁\", \"进程调度\", \"逻辑时钟\", \"DMA\"]', 'A', '互斥锁(Mutex)用于实现线程间的互斥访问，是常见的同步机制', 5, '线程同步', '同步机制', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (138, 3, 'single_choice', 'medium', '系统调用通常由哪种方式触发？', '[\"中断\", \"轮询\", \"直接调用硬件\", \"文件打开\"]', 'A', '系统调用通过软中断(陷阱)方式触发，从用户态切换到内核态', 5, '系统调用', '系统调用机制', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (139, 3, 'single_choice', 'easy', 'Linux 中查看当前进程的命令是：', '[\"ps\", \"ls\", \"top\", \"cat\"]', 'A', 'ps命令显示进程状态，top命令动态显示进程信息，ls列出文件', 5, 'Linux', 'Linux命令', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (140, 3, 'single_choice', 'easy', '下列存储访问速度最快的是：', '[\"磁盘\", \"内存\", \"CPU 寄存器\", \"闪存\"]', 'C', '存储器访问速度：寄存器 > 缓存 > 内存 > 磁盘。寄存器直接在CPU内部，速度最快', 5, '存储器', '存储层次', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (141, 3, 'multiple_choice', 'easy', '下列属于操作系统主要功能：', '[\"进程管理\", \"内存管理\", \"文件管理\", \"网络管理\"]', 'A,B,C', '操作系统的主要功能包括进程管理、内存管理、文件管理、设备管理。网络管理通常由网络协议栈实现', 10, '操作系统', '操作系统功能', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (142, 3, 'multiple_choice', 'medium', '产生死锁必须满足：', '[\"互斥\", \"不可剥夺\", \"请求与保持\", \"循环等待\"]', 'A,B,C,D', '死锁产生的四个必要条件：互斥条件、不可剥夺条件、请求与保持条件、循环等待条件', 10, '死锁', '死锁条件', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (143, 3, 'multiple_choice', 'medium', '解决临界区问题的要求包括：', '[\"互斥\", \"有限等待\", \"空闲让进\", \"死锁避免\"]', 'A,B,C', '临界区问题的三个要求：互斥(同一时刻只有一个进程在临界区)、空闲让进、有限等待', 10, '进程同步', '临界区要求', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (144, 3, 'multiple_choice', 'medium', '属于常用页面置换算法：', '[\"FIFO\", \"LRU\", \"OPT\", \"RR\"]', 'A,B,C', '常见页面置换算法：FIFO(先进先出)、LRU(最近最少使用)、OPT(最优置换)、LFU(最不经常使用)。RR是进程调度算法', 10, '内存管理', '页面置换算法', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (145, 3, 'multiple_choice', 'easy', '下列哪些是进程状态：', '[\"就绪\", \"阻塞\", \"运行\", \"延迟\"]', 'A,B,C', '进程的三态模型：运行态、就绪态、阻塞态(等待态)。五态模型增加了新建态和终止态', 10, '进程管理', '进程状态', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (146, 3, 'multiple_choice', 'medium', '以下属于系统调用：', '[\"open()\", \"read()\", \"write()\", \"for()\"]', 'A,B,C', 'open()、read()、write()都是系统调用，用于文件操作。for()是编程语言的循环语句', 10, '系统调用', '系统调用示例', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (147, 3, 'multiple_choice', 'hard', '下列哪些方法可防止死锁：', '[\"资源有序分配\", \"银行家算法\", \"破坏循环等待\", \"降低 CPU 主频\"]', 'A,B,C', '防止死锁的方法：破坏四个必要条件之一(如资源有序分配破坏循环等待)、使用银行家算法进行死锁避免', 10, '死锁', '死锁预防', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (148, 3, 'multiple_choice', 'medium', '多线程的优点包括：', '[\"提高系统吞吐量\", \"提高资源利用率\", \"降低 CPU 使用率\", \"改善响应速度\"]', 'A,B,D', '多线程的优点：提高系统吞吐量、提高资源利用率、改善响应速度、简化程序结构。不会降低CPU使用率', 10, '线程', '多线程优势', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (149, 3, 'multiple_choice', 'medium', 'I/O 管理中包括：', '[\"缓冲管理\", \"设备分配\", \"进程调度\", \"设备驱动程序\"]', 'A,B,D', 'I/O管理包括：缓冲管理、设备分配、设备驱动程序、中断处理等。进程调度属于进程管理', 10, 'I/O管理', 'I/O管理内容', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (150, 3, 'multiple_choice', 'medium', '虚拟存储技术的好处包括：', '[\"可扩充内存容量\", \"可运行大程序\", \"提高 CPU 运算速度\", \"提高内存利用率\"]', 'A,B,D', '虚拟内存的优点：扩充内存容量、可运行比物理内存大的程序、提高内存利用率。不能提高CPU运算速度', 10, '内存管理', '虚拟存储优点', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (151, 3, 'true_false', 'easy', '线程比进程更加轻量级。', NULL, '对', '线程是轻量级进程，创建和切换开销比进程小，共享进程资源', 5, '线程', '线程特点', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (152, 3, 'true_false', 'easy', '临界区必须保证互斥访问。', NULL, '对', '临界区是访问共享资源的代码段，必须保证互斥访问，同一时刻只允许一个进程进入', 5, '进程同步', '临界区互斥', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (153, 3, 'true_false', 'medium', 'SJF（最短作业优先）调度算法一定不会产生饥饿。', NULL, '错', 'SJF可能导致长作业饥饿，因为短作业会一直被优先执行', 5, '进程调度', 'SJF饥饿', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (154, 3, 'true_false', 'easy', '虚拟内存允许程序比物理内存大。', NULL, '对', '虚拟内存技术通过页面置换，使程序可以使用比物理内存更大的地址空间', 5, '内存管理', '虚拟内存特点', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (155, 3, 'true_false', 'medium', '死锁产生一定是因为循环等待条件。', NULL, '对', '死锁产生必须同时满足四个条件，其中循环等待是必要条件之一', 5, '死锁', '死锁条件', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (156, 3, 'true_false', 'medium', '操作系统可以通过禁止中断实现进程同步。', NULL, '对', '禁止中断可以实现临界区互斥，但这种方法开销大，只适用于内核态且时间很短的临界区', 5, '进程同步', '中断同步', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (157, 3, 'true_false', 'easy', '多道程序设计可以提高 CPU 利用率。', NULL, '对', '多道程序设计使多个程序并发执行，当一个程序等待I/O时CPU可执行其他程序，提高了CPU利用率', 5, '操作系统', '多道程序', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (158, 3, 'true_false', 'medium', 'FIFO 页面替换算法性能最优。', NULL, '错', 'FIFO算法简单但性能不是最优，可能产生Belady异常。OPT算法理论最优但无法实现', 5, '内存管理', 'FIFO算法', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (159, 3, 'true_false', 'medium', '互斥锁和信号量都可以实现同步。', NULL, '对', '互斥锁主要用于互斥，信号量既可实现互斥也可实现同步', 5, '进程同步', '同步机制', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (160, 3, 'true_false', 'easy', '系统调用需要从用户态切换到内核态。', NULL, '对', '系统调用是用户程序请求操作系统服务的接口，需要从用户态切换到内核态执行', 5, '系统调用', '态切换', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (161, 3, 'true_false', 'easy', '进程可以没有 PCB。', NULL, '错', 'PCB(进程控制块)是进程存在的唯一标志，每个进程都必须有PCB', 5, '进程管理', 'PCB', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (162, 3, 'true_false', 'medium', '只要使用了银行家算法就绝不会死锁。', NULL, '错', '银行家算法可以避免死锁，但只有在正确实现且满足算法前提条件时才有效', 5, '死锁', '银行家算法', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (163, 3, 'true_false', 'medium', '文件系统中的目录也是一种文件。', NULL, '对', '在Unix/Linux系统中，目录本身也是一种特殊的文件，存储了文件名和inode的对应关系', 5, '文件系统', '目录文件', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (164, 3, 'true_false', 'medium', '硬链接和软链接完全等价。', NULL, '错', '硬链接是文件的另一个名字，指向同一个inode；软链接(符号链接)是指向文件路径的指针，是不同的文件', 5, '文件系统', '链接类型', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (165, 3, 'true_false', 'medium', 'LRU 页面置换算法利用局部性原理。', NULL, '对', 'LRU(最近最少使用)算法基于程序的时间局部性原理：最近使用过的页面很可能在不久的将来再次被使用', 5, '内存管理', 'LRU原理', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (166, 3, 'fill_blank', 'easy', '进程控制块的英文缩写是______。', NULL, 'PCB', 'PCB(Process Control Block)进程控制块，是操作系统管理进程的数据结构', 10, '进程管理', 'PCB', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (167, 3, 'fill_blank', 'medium', '线程共享同一进程的______空间。', NULL, '地址', '同一进程的多个线程共享进程的地址空间、代码段、数据段等资源', 10, '线程', '线程共享', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (168, 3, 'fill_blank', 'medium', '产生死锁必须满足的四个条件是互斥、不可剥夺、请求与保持、______。', NULL, '循环等待', '死锁的四个必要条件：互斥、不可剥夺(不可抢占)、请求与保持、循环等待', 10, '死锁', '死锁条件', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (169, 3, 'fill_blank', 'medium', '虚拟内存主要依赖______技术实现。', NULL, '页式存储', '虚拟内存通过页式存储(分页)和页面置换技术实现，将程序按页划分，按需调入内存', 10, '内存管理', '虚拟内存技术', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (170, 3, 'fill_blank', 'easy', 'Linux 中查看当前目录文件使用命令______。', NULL, 'ls', 'ls命令用于列出目录内容，常用参数：-l(详细信息)、-a(显示隐藏文件)', 10, 'Linux', 'Linux命令', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (171, 3, 'fill_blank', 'easy', '操作系统内核的运行模式是______。', NULL, '内核态', '操作系统分为内核态(特权模式)和用户态，内核态具有最高权限', 10, '操作系统', '运行模式', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (172, 3, 'fill_blank', 'medium', '调度算法中采用固定时间片的方法是______。', NULL, 'RR', 'RR(Round Robin)时间片轮转算法，每个进程分配固定时间片，轮流执行', 10, '进程调度', '时间片轮转', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (173, 3, 'fill_blank', 'medium', '发生缺页时，系统会产生一个______中断。', NULL, '缺页', '缺页中断(Page Fault)在访问的页面不在内存时产生，触发页面调入操作', 10, '内存管理', '缺页中断', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (174, 3, 'fill_blank', 'medium', '文件系统中记录文件基本信息的数据结构叫做______。', NULL, 'inode', 'inode(索引节点)存储文件的元数据，如权限、大小、创建时间、数据块位置等', 10, '文件系统', 'inode', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (175, 3, 'fill_blank', 'hard', '解决临界区问题通常使用的同步工具包括互斥锁、信号量和______。', NULL, '管程', '管程(Monitor)是高级同步机制，将共享资源和对资源的操作封装在一起，保证互斥访问', 10, '进程同步', '同步工具', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (176, 3, 'fill_blank', 'medium', '系统调用使用______指令从用户态进入内核态。', NULL, '中断', '系统调用通过trap(陷阱)指令或软中断实现用户态到内核态的切换', 10, '系统调用', '系统调用机制', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (177, 3, 'fill_blank', 'medium', 'SJF 调度算法需要估计进程的______。', NULL, '执行时间', 'SJF(最短作业优先)需要预测进程的执行时间，选择执行时间最短的进程优先执行', 10, '进程调度', 'SJF算法', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (178, 3, 'fill_blank', 'medium', '段式管理中，段是按照______划分的。', NULL, '逻辑结构', '段式管理按程序的逻辑结构划分(如代码段、数据段、堆栈段)，段大小可变', 10, '内存管理', '段式管理', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (179, 3, 'fill_blank', 'medium', '文件按目录结构组织属于______文件系统结构。', NULL, '层次式', '层次式(树形)文件系统是最常见的文件组织方式，文件和目录以树形结构组织', 10, '文件系统', '文件组织', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (180, 3, 'fill_blank', 'easy', '在 Linux 中结束一个进程可使用命令______。', NULL, 'kill', 'kill命令向进程发送信号，如kill -9 pid强制终止进程', 10, 'Linux', 'Linux命令', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (181, 4, 'single_choice', 'easy', '线性表顺序存储结构的最大优点是：', '[\"插入删除方便\", \"不需要占用连续空间\", \"随机存取方便\", \"链接灵活\"]', 'C', '顺序存储结构支持随机访问，时间复杂度为O(1)，这是其最大优点。但插入删除需要移动元素，效率较低', 5, '线性表', '顺序存储', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (182, 4, 'single_choice', 'medium', '单链表中查找第 i 个结点的时间复杂度为：', '[\"O(1)\", \"O(log n)\", \"O(n)\", \"O(n log n)\"]', 'C', '单链表需要从头结点开始顺序查找，平均时间复杂度为O(n)', 5, '链表', '查找复杂度', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (183, 4, 'single_choice', 'easy', '下列结构中，不属于线性结构的是：', '[\"队列\", \"栈\", \"数组\", \"树\"]', 'D', '树是非线性结构，具有一对多的层次关系。队列、栈、数组都是线性结构', 5, '数据结构', '线性结构', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (184, 4, 'single_choice', 'easy', '栈的特点是：', '[\"FIFO\", \"FILO/LIFO\", \"随机访问\", \"任意插入删除\"]', 'B', '栈是后进先出(LIFO/FILO)的数据结构，只能在栈顶进行插入和删除操作', 5, '栈', '栈特性', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (185, 4, 'single_choice', 'medium', '循环队列队满的判断条件是：', '[\"front = rear\", \"(rear + 1) % MaxSize = front\", \"rear = MaxSize\", \"front = MaxSize\"]', 'B', '循环队列通过牺牲一个存储单元来区分队满和队空。队满条件：(rear + 1) % MaxSize == front', 5, '队列', '循环队列', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (186, 4, 'single_choice', 'hard', '二叉树前序遍历序列为 ABC，后序为 BCA，则中序遍历为：', '[\"ABC\", \"BAC\", \"BCA\", \"CAB\"]', 'B', '根据前序(根左右)ABC可知A是根，后序(左右根)BCA可知BC是子树。推导得中序为BAC', 5, '二叉树', '遍历推导', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (187, 4, 'single_choice', 'medium', '具有 n 个结点的完全二叉树的高度为：', '[\"log₂ n\", \"⌊log₂ n⌋ + 1\", \"n - 1\", \"n\"]', 'B', '完全二叉树的高度h = ⌊log₂ n⌋ + 1，其中n为节点数', 5, '二叉树', '完全二叉树', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (188, 4, 'single_choice', 'medium', '哈夫曼树的特点是：', '[\"完全二叉树\", \"带权路径长度最小\", \"路径长度相等\", \"所有叶子在同一层\"]', 'B', '哈夫曼树(最优二叉树)的特点是带权路径长度(WPL)最小，常用于数据压缩编码', 5, '哈夫曼树', 'WPL', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (189, 4, 'single_choice', 'medium', '深度优先遍历（DFS）常用的数据结构是：', '[\"栈\", \"队列\", \"数组\", \"哈希表\"]', 'A', 'DFS使用栈(递归或显式栈)实现，先访问深层节点。BFS使用队列实现', 5, '图', 'DFS实现', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (190, 4, 'single_choice', 'medium', '广度优先遍历（BFS）常用：', '[\"栈\", \"队列\", \"链表\", \"堆\"]', 'B', 'BFS使用队列实现，按层次遍历，先访问的节点先处理', 5, '图', 'BFS实现', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (191, 4, 'single_choice', 'medium', '在顺序查找中，平均查找长度为：', '[\"1\", \"n\", \"(n + 1) / 2\", \"log₂ n\"]', 'C', '顺序查找的平均查找长度ASL = (n + 1) / 2，最好O(1)，最坏O(n)', 5, '查找', '顺序查找', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (192, 4, 'single_choice', 'medium', '下列排序中，最坏时间复杂度为 O(n²) 的是：', '[\"快速排序\", \"堆排序\", \"冒泡排序\", \"归并排序\"]', 'C', '冒泡排序、插入排序、选择排序最坏情况都是O(n²)。快速排序最坏O(n²)但平均O(n log n)', 5, '排序', '时间复杂度', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (193, 4, 'single_choice', 'medium', '在快速排序中，关键操作是：', '[\"归并\", \"建堆\", \"枢轴划分\", \"插入\"]', 'C', '快速排序的核心是partition操作，选择枢轴(pivot)将数组划分为两部分', 5, '排序', '快排原理', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (194, 4, 'single_choice', 'medium', '图的邻接矩阵适合用于：', '[\"稀疏图\", \"稠密图\", \"大规模稀疏图\", \"只包含树结构\"]', 'B', '邻接矩阵空间复杂度O(n²)，适合稠密图。稀疏图适合用邻接表，空间O(n+e)', 5, '图', '存储结构', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (195, 4, 'single_choice', 'easy', '一个含 n 个结点的树有多少条边：', '[\"n\", \"n−1\", \"n+1\", \"2n\"]', 'B', '树的性质：n个结点的树有n-1条边。这是树的基本性质', 5, '树', '树的性质', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (196, 4, 'single_choice', 'medium', '哈希表中的冲突解决方法包括：', '[\"顺序查找\", \"链地址法\", \"折叠法\", \"平方探测法\"]', 'B', '哈希冲突解决方法：链地址法(拉链法)、开放定址法(线性探测、平方探测、双散列)、再散列法等', 5, '哈希表', '冲突处理', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (197, 4, 'single_choice', 'medium', '下列排序算法中属于稳定排序的是：', '[\"堆排序\", \"希尔排序\", \"归并排序\", \"快速排序\"]', 'C', '稳定排序：冒泡、插入、归并。不稳定排序：选择、快速、堆、希尔', 5, '排序', '稳定性', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (198, 4, 'single_choice', 'medium', '一个二叉树的叶子结点数为 n0，度为 2 的结点数为 n2，则：', '[\"n0 = n2\", \"n0 = n2 + 1\", \"n0 = 2n2\", \"n0 = 3n2\"]', 'B', '二叉树的性质：叶子结点数 = 度为2的结点数 + 1，即 n0 = n2 + 1', 5, '二叉树', '节点关系', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (199, 4, 'single_choice', 'easy', '对栈顶指针 top，若入栈一个元素，则执行：', '[\"top--\", \"top++\", \"top = 0\", \"top = -1\"]', 'B', '入栈操作：先top++，再存入元素；或先存入元素，再top++', 5, '栈', '栈操作', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (200, 4, 'single_choice', 'easy', '一个非空队列出队时，front 的变化是：', '[\"front++\", \"front--\", \"front=0\", \"front 不变\"]', 'A', '队列出队操作：先取出front指向的元素，然后front++(循环队列需要取模)', 5, '队列', '队列操作', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (201, 4, 'multiple_choice', 'easy', '数组的特点包括：', '[\"随机存取\", \"插入删除效率高\", \"存储空间连续\", \"支持顺序存储\"]', 'A,C,D', '数组特点：随机存取O(1)、存储空间连续、支持顺序存储。插入删除需要移动元素，效率低', 10, '数组', '数组特性', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (202, 4, 'multiple_choice', 'easy', '下列属于树的遍历方式：', '[\"前序\", \"中序\", \"后序\", \"层序\"]', 'A,B,C,D', '二叉树遍历方式：前序(根左右)、中序(左根右)、后序(左右根)、层序(逐层遍历)', 10, '二叉树', '遍历方式', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (203, 4, 'multiple_choice', 'medium', '哈夫曼编码的特点：', '[\"前缀编码\", \"不唯一\", \"最优编码\", \"所有编码长度相同\"]', 'A,B,C', '哈夫曼编码特点：前缀编码(无歧义)、最优编码(带权路径长度最小)、不唯一(构造过程的选择导致)、变长编码', 10, '哈夫曼树', '编码特性', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (204, 4, 'multiple_choice', 'medium', '能实现稳定排序的有：', '[\"冒泡\", \"插入\", \"选择\", \"归并\"]', 'A,B,D', '稳定排序(相同元素相对位置不变)：冒泡、插入、归并、基数排序。选择排序不稳定', 10, '排序', '稳定性', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (205, 4, 'multiple_choice', 'easy', '下列哪些属于线性结构：', '[\"栈\", \"队列\", \"图\", \"链表\"]', 'A,B,D', '线性结构：数组、链表、栈、队列、串。非线性结构：树、图', 10, '数据结构', '线性 vs 非线性', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (206, 4, 'multiple_choice', 'medium', '图的存储方式包括：', '[\"邻接矩阵\", \"邻接表\", \"十字链表\", \"邻接树\"]', 'A,B,C', '图的存储方式：邻接矩阵、邻接表、十字链表(有向图)、邻接多重表(无向图)', 10, '图', '存储结构', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (207, 4, 'multiple_choice', 'medium', '哈希冲突解决方法包括：', '[\"开放定址法\", \"拉链法\", \"十字链法\", \"再哈希法\"]', 'A,B,D', '哈希冲突解决：开放定址法(线性探测、平方探测)、拉链法(链地址法)、再哈希法、建立公共溢出区', 10, '哈希表', '冲突处理', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (208, 4, 'multiple_choice', 'medium', '以下哪些排序算法平均时间复杂度是 O(n log n)：', '[\"选择排序\", \"归并排序\", \"堆排序\", \"快速排序\"]', 'B,C,D', '时间复杂度O(n log n)的排序：归并、堆、快速(平均)。选择排序是O(n²)', 10, '排序', '时间复杂度', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (209, 4, 'multiple_choice', 'medium', '深度优先遍历（DFS）的特点：', '[\"使用栈\", \"优先访问未访问的邻点\", \"与树的前序遍历类似\", \"使用队列\"]', 'A,B,C', 'DFS特点：使用栈或递归、优先访问深层节点、类似树的前序遍历。BFS使用队列', 10, '图', 'DFS特性', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (210, 4, 'multiple_choice', 'medium', '对于循环队列，下列说法正确的是：', '[\"rear 指向队尾元素\", \"front 指向队首元素\", \"入队 rear 后移\", \"出队 front 后移\"]', 'C,D', '循环队列：front指向队首元素的前一个位置，rear指向队尾元素。入队rear后移，出队front后移', 10, '队列', '循环队列', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (211, 4, 'true_false', 'easy', '链表需要连续内存空间。', NULL, '错', '链表不需要连续内存空间，通过指针链接各节点。数组需要连续内存空间', 5, '链表', '存储特性', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (212, 4, 'true_false', 'easy', '栈只能在一端进行操作。', NULL, '对', '栈是受限的线性表，只能在栈顶(一端)进行插入和删除操作', 5, '栈', '栈特性', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (213, 4, 'true_false', 'easy', '队列是一种先进后出的结构。', NULL, '错', '队列是先进先出(FIFO)的结构，栈是先进后出(FILO/LIFO)', 5, '队列', '队列特性', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (214, 4, 'true_false', 'easy', '二叉树中每个结点度最多为 2。', NULL, '对', '二叉树的定义：每个节点最多有两个子节点(左子节点和右子节点)', 5, '二叉树', '基本概念', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (215, 4, 'true_false', 'easy', '叶子结点度为 0。', NULL, '对', '叶子结点(终端节点)没有子节点，度为0', 5, '二叉树', '节点度', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (216, 4, 'true_false', 'medium', '二叉树一定是有序树。', NULL, '错', '二叉树不一定有序。二叉搜索树(BST)才是有序的(左子树 < 根 < 右子树)', 5, '二叉树', '有序性', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (217, 4, 'true_false', 'medium', '平衡二叉树的左右子树高度差不超过 1。', NULL, '对', 'AVL树(平衡二叉树)要求任意节点的左右子树高度差不超过1，保证查找效率', 5, '平衡二叉树', 'AVL性质', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (218, 4, 'true_false', 'medium', '二叉搜索树中序遍历是升序序列。', NULL, '对', '二叉搜索树(BST)的中序遍历结果是递增有序序列，这是BST的重要性质', 5, '二叉搜索树', '遍历性质', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (219, 4, 'true_false', 'medium', '哈希表查找一般时间复杂度为 O(1)。', NULL, '对', '理想情况下哈希表查找时间为O(1)。但有冲突时可能退化到O(n)', 5, '哈希表', '查找效率', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (220, 4, 'true_false', 'easy', '图一定是连通的。', NULL, '错', '图不一定连通。连通图是指任意两个顶点都有路径相连的无向图', 5, '图', '连通性', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (221, 4, 'true_false', 'medium', '拓扑排序适用于有向无环图（DAG）。', NULL, '对', '拓扑排序只能用于有向无环图(DAG)。有环图无法进行拓扑排序', 5, '图', '拓扑排序', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (222, 4, 'true_false', 'medium', '堆可以看作完全二叉树。', NULL, '对', '堆是完全二叉树，分为大根堆(父节点≥子节点)和小根堆(父节点≤子节点)', 5, '堆', '堆结构', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (223, 4, 'true_false', 'medium', 'BFS 与 DFS 都可用于判断图是否连通。', NULL, '对', 'BFS和DFS都可以遍历图，通过判断是否访问到所有节点来确定图的连通性', 5, '图', '图遍历应用', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (224, 4, 'true_false', 'medium', '快速排序在最坏情况下时间复杂度是 O(n²)。', NULL, '对', '快速排序最坏情况O(n²)(每次划分极不均匀)，平均O(n log n)，最好O(n log n)', 5, '排序', '快排复杂度', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (225, 4, 'true_false', 'medium', '冒泡排序每一轮都能确定一个最大或最小值。', NULL, '对', '冒泡排序每轮比较相邻元素并交换，将最大(或最小)元素冒泡到末尾，确定一个元素的最终位置', 5, '排序', '冒泡排序', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (226, 4, 'fill_blank', 'easy', '栈的基本操作包括 push 和______。', NULL, 'pop', '栈的基本操作：push(入栈)、pop(出栈)、top/peek(查看栈顶)、isEmpty(判空)', 10, '栈', '栈操作', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (227, 4, 'fill_blank', 'easy', '队列是一种______结构。', NULL, 'FIFO;先进先出', '队列是先进先出(FIFO - First In First Out)的线性数据结构', 10, '队列', '队列特性', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (228, 4, 'fill_blank', 'easy', '单链表中每个结点至少包含数据域和______域。', NULL, '指针;next', '链表节点包含：数据域(存储数据)和指针域(指向下一个节点)', 10, '链表', '链表结构', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (229, 4, 'fill_blank', 'easy', '二叉树的三种深度遍历包括前序、中序和______。', NULL, '后序', '二叉树深度遍历：前序(根左右)、中序(左根右)、后序(左右根)', 10, '二叉树', '遍历方式', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (230, 4, 'fill_blank', 'medium', '排序算法中，以\"分治\"思想为基础的是______。', NULL, '快速排序;归并排序', '基于分治思想的排序：快速排序(划分后递归)、归并排序(分解后合并)', 10, '排序', '分治算法', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (231, 4, 'fill_blank', 'medium', '图的广度优先遍历需要使用的数据结构是______。', NULL, '队列', 'BFS使用队列，按层次遍历；DFS使用栈或递归', 10, '图', 'BFS', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (232, 4, 'fill_blank', 'medium', '完全二叉树第 i 层最多有______个结点。', NULL, '2^(i-1)', '完全二叉树第i层最多有2^(i-1)个节点(i从1开始计数)', 10, '二叉树', '完全二叉树性质', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (233, 4, 'fill_blank', 'medium', '哈夫曼树最优编码的特点是______编码。', NULL, '前缀', '哈夫曼编码是前缀编码，任何一个编码都不是另一个编码的前缀，保证解码唯一性', 10, '哈夫曼树', '哈夫曼编码', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (234, 4, 'fill_blank', 'medium', '二叉搜索树中，任意节点左子树的值都______根节点的值。', NULL, '小于', '二叉搜索树(BST)性质：左子树所有节点 < 根节点 < 右子树所有节点', 10, '二叉搜索树', 'BST性质', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (235, 4, 'fill_blank', 'medium', '哈希表中用于减少冲突的方法称为______。', NULL, '冲突解决;冲突处理', '哈希冲突解决方法：开放定址法、链地址法、再哈希法等', 10, '哈希表', '哈希冲突', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (236, 4, 'fill_blank', 'easy', 'BFS 与 DFS 分别属于______搜索与______搜索。', NULL, '广度;深度', 'BFS(Breadth-First Search)广度优先搜索，DFS(Depth-First Search)深度优先搜索', 10, '图', '图遍历', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (237, 4, 'fill_blank', 'medium', '堆排序中使用的堆是______堆或______堆。', NULL, '大根;小根', '堆排序使用大根堆(升序)或小根堆(降序)。大根堆：父节点≥子节点', 10, '堆', '堆结构', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (238, 4, 'fill_blank', 'medium', '图的邻接矩阵空间复杂度为______。', NULL, 'O(n²)', '邻接矩阵是n×n的二维数组，空间复杂度O(n²)。邻接表空间复杂度O(n+e)', 10, '图', '存储结构', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (239, 4, 'fill_blank', 'medium', '在顺序查找中，平均查找长度为______。', NULL, '(n + 1) / 2', '顺序查找平均查找长度ASL = (1+2+...+n)/n = (n+1)/2', 10, '查找', '顺序查找', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (240, 4, 'fill_blank', 'medium', '二叉树叶子结点数 n0 与度为 2 的结点数 n2 满足关系______。', NULL, 'n0 = n2 + 1', '二叉树性质：叶子结点数 = 度为2的结点数 + 1', 10, '二叉树', '二叉树性质', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (241, 5, 'single_choice', 'easy', '数据库系统（DBMS）的主要功能不包括：', '[\"数据定义\", \"数据操纵\", \"操作系统管理\", \"数据控制\"]', 'C', 'DBMS的主要功能包括：数据定义(DDL)、数据操纵(DML)、数据控制(DCL)、数据库维护。操作系统管理不属于DBMS功能', 5, 'DBMS基础', 'DBMS功能', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (242, 5, 'single_choice', 'easy', '关系数据库中，一个表对应一个______。', '[\"关系\", \"元组\", \"属性\", \"键\"]', 'A', '关系数据库中，表就是关系，行是元组，列是属性', 5, '关系模型', '基本概念', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (243, 5, 'single_choice', 'easy', '在关系表中，一行数据称为：', '[\"属性\", \"元组\", \"关系\", \"列\"]', 'B', '关系表中：行称为元组(tuple)，列称为属性(attribute)', 5, '关系模型', '元组', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (244, 5, 'single_choice', 'medium', '候选键的特点是：', '[\"能唯一标识元组\", \"由多个属性组成\", \"必须是主键\", \"必须是外键\"]', 'A', '候选键能唯一标识元组，且是最小的属性集。主键是从候选键中选择的一个', 5, '关系模型', '候选键', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (245, 5, 'single_choice', 'easy', 'SQL 中用于从表中查询数据的语句是：', '[\"INSERT\", \"DELETE\", \"UPDATE\", \"SELECT\"]', 'D', 'SQL的DML语句：SELECT(查询)、INSERT(插入)、UPDATE(更新)、DELETE(删除)', 5, 'SQL基础', 'SELECT语句', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (246, 5, 'single_choice', 'easy', '用于删除整个表结构及数据的命令是：', '[\"DROP\", \"DELETE\", \"REMOVE\", \"ERASE\"]', 'A', 'DROP删除表结构和数据，DELETE只删除数据保留结构，TRUNCATE删除所有数据但保留结构', 5, 'SQL DDL', 'DROP', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (247, 5, 'single_choice', 'easy', 'WHERE 子句在 SQL 中用于：', '[\"指定排序规则\", \"指定查询条件\", \"指定分组字段\", \"指定表名\"]', 'B', 'WHERE子句用于指定查询条件，过滤数据。ORDER BY用于排序，GROUP BY用于分组', 5, 'SQL查询', 'WHERE子句', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (248, 5, 'single_choice', 'easy', 'SQL 聚合函数不包括：', '[\"SUM\", \"AVG\", \"COUNT\", \"PRINT\"]', 'D', 'SQL聚合函数包括：SUM(求和)、AVG(平均值)、COUNT(计数)、MAX(最大值)、MIN(最小值)', 5, 'SQL函数', '聚合函数', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (249, 5, 'single_choice', 'medium', '数据库三范式中，第二范式（2NF）要求：', '[\"消除部分函数依赖\", \"消除传递依赖\", \"消除多值依赖\", \"消除主属性\"]', 'A', '1NF消除重复组；2NF消除部分函数依赖(非主属性完全依赖主键)；3NF消除传递依赖', 5, '规范化', '2NF', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (250, 5, 'single_choice', 'medium', '事务具有不可分割性，这个特性称为：', '[\"一致性\", \"隔离性\", \"原子性\", \"持久性\"]', 'C', 'ACID特性：原子性(Atomicity-不可分割)、一致性(Consistency)、隔离性(Isolation)、持久性(Durability)', 5, '事务', 'ACID', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (251, 5, 'single_choice', 'medium', '防止丢失更新属于事务的______问题。', '[\"死锁\", \"并发\", \"设计\", \"存储\"]', 'B', '事务并发问题：丢失更新、脏读、不可重复读、幻读', 5, '并发控制', '并发问题', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (252, 5, 'single_choice', 'easy', '索引的主要作用是：', '[\"增加磁盘使用\", \"提高查询效率\", \"降低更新效率\", \"提高存储空间\"]', 'B', '索引的优点是提高查询效率，缺点是占用额外存储空间，降低插入、更新、删除效率', 5, '索引', '索引作用', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (253, 5, 'single_choice', 'medium', 'B+ 树索引通常用于：', '[\"全表扫描\", \"提高数据插入速度\", \"范围查询\", \"事务控制\"]', 'C', 'B+树索引特点：所有数据在叶子节点，叶子节点间有指针连接，适合范围查询和顺序访问', 5, '索引', 'B+树', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (254, 5, 'single_choice', 'easy', 'SQL 中用于对结果排序的子句是：', '[\"GROUP BY\", \"ORDER BY\", \"SORT\", \"SEQUENCE\"]', 'B', 'ORDER BY子句用于排序，ASC升序(默认)，DESC降序', 5, 'SQL查询', 'ORDER BY', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (255, 5, 'single_choice', 'medium', '下列哪个不是视图的特点：', '[\"可以减少数据冗余\", \"能提高安全性\", \"可存储数据\", \"可简化复杂查询\"]', 'C', '视图是虚拟表，不存储数据，只保存查询定义。优点：简化查询、提高安全性、逻辑数据独立性', 5, '视图', '视图特性', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (256, 5, 'single_choice', 'medium', '外键用于：', '[\"限制列取值范围\", \"保证引用完整性\", \"提高查询速度\", \"做表连接\"]', 'B', '外键用于保证引用完整性，确保外键值必须在主表的主键中存在或为NULL', 5, '完整性约束', '外键', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (257, 5, 'single_choice', 'medium', 'SQL 中表示 NULL 值的判断使用：', '[\"= NULL\", \"!= NULL\", \"IS NULL\", \"== NULL\"]', 'C', 'NULL的判断必须使用IS NULL或IS NOT NULL，不能使用=或!=比较', 5, 'SQL基础', 'NULL处理', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (258, 5, 'single_choice', 'medium', '触发器在数据库中用于：', '[\"提高系统性能\", \"自动执行特定操作\", \"备份数据库\", \"提高磁盘利用率\"]', 'B', '触发器是特殊的存储过程，在特定事件(INSERT、UPDATE、DELETE)发生时自动执行', 5, '数据库对象', '触发器', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (259, 5, 'single_choice', 'medium', '数据库恢复技术中，利用日志进行恢复的是：', '[\"Checkpoint\", \"回滚（ROLLBACK）\", \"日志恢复\", \"数据复制\"]', 'C', '数据库恢复技术：日志恢复、检查点(Checkpoint)、数据备份。日志记录了所有事务操作，用于故障恢复', 5, '事务与恢复', '日志恢复', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (260, 5, 'single_choice', 'easy', 'SQL 中用于连接两个表的操作是：', '[\"MERGE\", \"JOIN\", \"UNION\", \"COMBINE\"]', 'B', 'JOIN用于连接表(INNER JOIN、LEFT JOIN、RIGHT JOIN、FULL JOIN)，UNION用于合并结果集', 5, 'SQL查询', 'JOIN', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (261, 5, 'multiple_choice', 'medium', '数据库的三级模式包括：', '[\"概念模式\", \"外模式\", \"内模式\", \"逻辑模式\"]', 'A,B,C', '数据库三级模式：外模式(用户视图)、概念模式(逻辑模式)、内模式(物理模式)。提供两级映像保证数据独立性', 10, '数据库体系结构', '三级模式', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (262, 5, 'multiple_choice', 'easy', 'SQL 中常见的约束包括：', '[\"PRIMARY KEY\", \"UNIQUE\", \"FOREIGN KEY\", \"NONNULL\"]', 'A,B,C', 'SQL约束：PRIMARY KEY(主键)、FOREIGN KEY(外键)、UNIQUE(唯一)、NOT NULL(非空)、CHECK(检查)、DEFAULT(默认值)', 10, '完整性约束', 'SQL约束', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (263, 5, 'multiple_choice', 'medium', '事务的 ACID 特性包含：', '[\"原子性\", \"一致性\", \"隔离性\", \"持久性\"]', 'A,B,C,D', 'ACID特性：原子性(Atomicity)、一致性(Consistency)、隔离性(Isolation)、持久性(Durability)', 10, '事务', 'ACID', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (264, 5, 'multiple_choice', 'medium', '会造成并发冲突的问题包括：', '[\"脏读\", \"不可重复读\", \"丢失更新\", \"幻读\"]', 'A,B,C,D', '并发问题：丢失更新(两个事务同时修改)、脏读(读未提交数据)、不可重复读(前后读取不一致)、幻读(结果集变化)', 10, '并发控制', '并发问题', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (265, 5, 'multiple_choice', 'easy', 'SQL 聚合函数包括：', '[\"SUM\", \"MIN\", \"MAX\", \"AVG\"]', 'A,B,C,D', 'SQL聚合函数：SUM(求和)、AVG(平均)、COUNT(计数)、MAX(最大)、MIN(最小)', 10, 'SQL函数', '聚合函数', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (266, 5, 'multiple_choice', 'medium', '下列属于连接查询的是：', '[\"内连接\", \"左连接\", \"右连接\", \"全外连接\"]', 'A,B,C,D', '连接类型：内连接(INNER JOIN)、左外连接(LEFT JOIN)、右外连接(RIGHT JOIN)、全外连接(FULL JOIN)、交叉连接(CROSS JOIN)', 10, 'SQL查询', 'JOIN类型', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (267, 5, 'multiple_choice', 'medium', '规范化的目的包括：', '[\"消除数据冗余\", \"避免数据异常\", \"减少数据存储成本\", \"提高扩展性\"]', 'A,B,D', '规范化目的：消除数据冗余、避免插入/删除/更新异常、提高数据一致性和可维护性', 10, '规范化', '设计目标', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (268, 5, 'multiple_choice', 'medium', '存储过程的优点包括：', '[\"提高执行效率\", \"提高安全性\", \"减少网络传输\", \"增加存储空间\"]', 'A,B,C', '存储过程优点：预编译提高效率、减少网络传输、提高安全性(权限控制)、代码重用。缺点：占用额外存储空间、调试困难', 10, '数据库编程', '存储过程', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (269, 5, 'multiple_choice', 'medium', '数据库索引的类型包括：', '[\"B+树索引\", \"哈希索引\", \"全文索引\", \"二叉搜索树索引\"]', 'A,B,C', '索引类型：B+树索引(最常用)、哈希索引(等值查询快)、全文索引(文本搜索)、位图索引等', 10, '索引', '索引类型', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (270, 5, 'multiple_choice', 'medium', '数据库备份方式包括：', '[\"完全备份\", \"差异备份\", \"日志备份\", \"灾备复制\"]', 'A,B,C,D', '备份方式：完全备份(全量)、差异备份(与上次完全备份的差异)、增量备份(与上次备份的差异)、日志备份(事务日志)', 10, '数据库管理', '备份策略', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (271, 5, 'true_false', 'easy', '主键可以重复。', NULL, '错', '主键必须唯一且非空，用于唯一标识表中的每一行', 5, '完整性约束', '主键', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (272, 5, 'true_false', 'medium', '外键允许取 NULL。', NULL, '对', '外键可以为NULL(表示没有关联)，但如果不为NULL则必须在主表的主键中存在', 5, '完整性约束', '外键', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (273, 5, 'true_false', 'medium', 'WHERE 子句在 GROUP BY 之后执行。', NULL, '错', 'SQL执行顺序：FROM → WHERE → GROUP BY → HAVING → SELECT → ORDER BY。WHERE在分组前过滤，HAVING在分组后过滤', 5, 'SQL查询', '执行顺序', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (274, 5, 'true_false', 'easy', 'SQL 查询默认不去重。', NULL, '对', 'SELECT默认返回所有行包括重复行，使用DISTINCT关键字去重', 5, 'SQL基础', '去重', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (275, 5, 'true_false', 'medium', '聚簇索引将索引和数据放在一起存储。', NULL, '对', '聚簇索引(Clustered Index)的叶子节点存储完整数据行，非聚簇索引叶子节点存储主键值', 5, '索引', '聚簇索引', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (276, 5, 'true_false', 'medium', '视图可以基于多张表创建。', NULL, '对', '视图可以基于一个或多个表，甚至基于其他视图创建', 5, '视图', '视图定义', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (277, 5, 'true_false', 'medium', 'NULL 与任何值比较结果都是 FALSE。', NULL, '错', 'NULL与任何值比较(包括NULL)结果都是UNKNOWN(不是TRUE也不是FALSE)。必须使用IS NULL判断', 5, 'SQL基础', 'NULL语义', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (278, 5, 'true_false', 'easy', '触发器可以自动执行。', NULL, '对', '触发器在INSERT、UPDATE、DELETE等事件发生时自动触发执行，无需手动调用', 5, '数据库对象', '触发器', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (279, 5, 'true_false', 'medium', '在可重复读级别下，脏读被禁止。', NULL, '对', '隔离级别从低到高：读未提交(脏读)、读已提交(不可重复读)、可重复读(幻读)、串行化(无并发问题)', 5, '事务', '隔离级别', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (280, 5, 'true_false', 'medium', '数据库日志可用于恢复事务。', NULL, '对', '事务日志记录所有数据修改操作，用于REDO(重做)和UNDO(撤销)操作，实现故障恢复', 5, '事务与恢复', '日志', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (281, 5, 'true_false', 'medium', '规范化完全可以消除所有数据冗余。', NULL, '错', '规范化可以大幅减少冗余，但不能完全消除。实际应用中有时需要反规范化以提高性能', 5, '规范化', '设计局限', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (282, 5, 'true_false', 'medium', '索引会加快所有 SQL 的执行。', NULL, '错', '索引可以加快查询，但会降低INSERT、UPDATE、DELETE的速度。小表全表扫描可能比使用索引更快', 5, '索引', '索引代价', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (283, 5, 'true_false', 'medium', '外键必须引用主键。', NULL, '对', '外键通常引用主键，也可以引用唯一键(UNIQUE约束的列)', 5, '完整性约束', '外键引用', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (284, 5, 'true_false', 'easy', 'DELETE 删除数据，但不会删除表结构。', NULL, '对', 'DELETE删除数据保留结构，TRUNCATE清空数据保留结构，DROP删除表结构和数据', 5, 'SQL DML', 'DELETE', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (285, 5, 'true_false', 'medium', '数据库的并发控制可以避免死锁。', NULL, '错', '并发控制(锁机制)可能导致死锁。数据库通过死锁检测和超时机制处理死锁', 5, '并发控制', '死锁', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (286, 5, 'fill_blank', 'easy', '数据库中，表的一行称为______。', NULL, '元组;tuple', '关系模型术语：关系(表)、元组(行)、属性(列)、域(属性取值范围)、键(唯一标识)', 10, '关系模型', '元组', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (287, 5, 'fill_blank', 'easy', 'SQL 中创建表使用的语句是______。', NULL, 'CREATE TABLE', 'DDL语句：CREATE(创建)、ALTER(修改)、DROP(删除)、TRUNCATE(截断)', 10, 'SQL DDL', '建表', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (288, 5, 'fill_blank', 'easy', '主键用于唯一标识______。', NULL, '元组;行', '主键(Primary Key)唯一标识表中的每一行，必须唯一且非空', 10, '完整性约束', '主键作用', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (289, 5, 'fill_blank', 'easy', '删除表中所有数据但不删除结构的命令是______。', NULL, 'DELETE;TRUNCATE', 'DELETE和TRUNCATE都可以删除数据保留结构。TRUNCATE速度快但不能回滚', 10, 'SQL DML', '清空数据', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (290, 5, 'fill_blank', 'easy', 'SQL 中用于给结果去重的关键字是______。', NULL, 'DISTINCT', 'SELECT DISTINCT 列名 FROM 表名; 用于去除重复行', 10, 'SQL查询', '去重', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (291, 5, 'fill_blank', 'easy', 'WHERE 子句用于指定______条件。', NULL, '查询;过滤', 'WHERE子句用于过滤数据，指定查询条件', 10, 'SQL查询', 'WHERE作用', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (292, 5, 'fill_blank', 'easy', '对表中数据进行排序使用______子句。', NULL, 'ORDER BY', 'ORDER BY 列名 ASC|DESC; 用于结果排序，ASC升序(默认)，DESC降序', 10, 'SQL查询', '排序', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (293, 5, 'fill_blank', 'easy', 'GROUP BY 子句用于______数据。', NULL, '分组', 'GROUP BY用于分组，通常与聚合函数配合使用，HAVING子句对分组结果进行过滤', 10, 'SQL查询', '分组', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (294, 5, 'fill_blank', 'medium', '事务的隔离性用于避免______冲突。', NULL, '并发', '隔离性确保并发事务相互隔离，避免脏读、不可重复读、幻读等并发问题', 10, '事务', '隔离性', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (295, 5, 'fill_blank', 'easy', '数据库中，为提高查询效率常创建______。', NULL, '索引', '索引是数据库优化的重要手段，可以显著提高查询速度', 10, '索引', '索引目的', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (296, 5, 'fill_blank', 'medium', 'B+树索引适用于______查询。', NULL, '范围', 'B+树索引适合范围查询、排序和分组。哈希索引只适合等值查询', 10, '索引', 'B+树应用', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (297, 5, 'fill_blank', 'easy', 'ROLLBACK 用于______事务。', NULL, '回滚', '事务控制：BEGIN/START TRANSACTION(开始)、COMMIT(提交)、ROLLBACK(回滚)', 10, '事务', '回滚', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (298, 5, 'fill_blank', 'medium', '提高数据库安全性的机制包括______。', NULL, '权限控制;访问控制', '数据库安全机制：用户认证、权限控制(GRANT/REVOKE)、视图、审计、加密', 10, '数据库安全', '安全机制', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');
INSERT INTO `tb_question_bank` VALUES (299, 5, 'fill_blank', 'easy', '数据库中 NULL 表示______。', NULL, '未知值;空值', 'NULL表示未知值或不存在的值，不同于空字符串\'或0', 10, 'SQL基础', 'NULL含义', 0, 1, '张教授', 'active', 1, '2025-11-26 22:33:04', '2025-12-18 19:51:15');
INSERT INTO `tb_question_bank` VALUES (300, 5, 'fill_blank', 'medium', '第三范式要求消除______依赖。', NULL, '传递', '1NF消除重复组；2NF消除部分依赖；3NF消除传递依赖(非主属性不依赖于其他非主属性)', 10, '规范化', '3NF', 0, 1, '张教授', 'active', 0, '2025-11-26 22:33:04', '2025-12-18 19:51:14');

-- ----------------------------
-- Table structure for tb_resource
-- ----------------------------
DROP TABLE IF EXISTS `tb_resource`;
CREATE TABLE `tb_resource`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名称(冗余)',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源名称',
  `type` enum('document','video','code','other') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资源类型',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '资源描述',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件URL(OSS)',
  `preview_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'PDF棰勮?鏂囦欢URL',
  `file_size` bigint NOT NULL COMMENT '文件大小(字节)',
  `uploader_id` bigint NOT NULL COMMENT '上传者ID',
  `uploader_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '上传者姓名(冗余)',
  `download_count` int NULL DEFAULT 0 COMMENT '下载次数',
  `tags` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标签(逗号分隔)',
  `status` enum('pending','approved','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '审核状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_type_status`(`type` ASC, `status` ASC) USING BTREE,
  INDEX `idx_uploader_id`(`uploader_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` DESC) USING BTREE,
  FULLTEXT INDEX `idx_fulltext_name`(`name`)
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '教学资源表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_resource
-- ----------------------------
INSERT INTO `tb_resource` VALUES (1, 1, 'Java程序设计', 'Java程序设计课件.pptx', 'document', 'Java程序设计完整教学课件', 'http://120.26.212.210/resources/ppt/Java程序设计课件.pptx', 'http://120.26.212.210/resources/ppt/Java程序设计课件.pdf', 5120000, 1, '张教授', 0, 'Java,课件,PPT', 'approved', '2025-12-04 23:05:20');
INSERT INTO `tb_resource` VALUES (2, 2, 'Web开发技术', 'Web开发技术课件.pptx', 'document', 'Web开发技术教学课件', 'http://120.26.212.210/resources/ppt/Web开发技术课件.pptx', 'http://120.26.212.210/resources/ppt/Web开发技术课件.pdf', 4560000, 3, '王讲师', 0, 'Web,课件,PPT', 'approved', '2025-12-04 23:05:20');
INSERT INTO `tb_resource` VALUES (3, 3, '操作系统', '操作系统课件.pptx', 'document', '操作系统原理教学课件', 'http://120.26.212.210/resources/ppt/操作系统课件.pptx', 'http://120.26.212.210/resources/ppt/操作系统课件.pdf', 6230000, 1, '张教授', 0, '操作系统,课件,PPT', 'approved', '2025-12-04 23:05:20');
INSERT INTO `tb_resource` VALUES (4, 4, '数据结构', '数据结构课件.pptx', 'document', '数据结构与算法教学课件', 'http://120.26.212.210/resources/ppt/数据结构课件.pptx', 'http://120.26.212.210/resources/ppt/数据结构课件.pdf', 5890000, 1, '张教授', 0, '数据结构,课件,PPT', 'approved', '2025-12-04 23:05:20');
INSERT INTO `tb_resource` VALUES (5, 5, '数据库原理', '数据库原理课件.pptx', 'document', '数据库原理与应用教学课件', 'http://120.26.212.210/resources/ppt/数据库原理课件.pptx', 'http://120.26.212.210/resources/ppt/数据库原理课件.pdf', 4980000, 2, '李老师', 0, '数据库,课件,PPT', 'approved', '2025-12-04 23:05:20');
INSERT INTO `tb_resource` VALUES (6, 1, 'Java程序设计', 'Java程序设计A卷.docx', 'document', 'Java程序设计期末试卷A卷', 'http://120.26.212.210/resources/testpaper/Java程序设计A卷.docx', 'http://120.26.212.210/resources/testpaper/Java程序设计A卷.pdf', 156000, 1, '张教授', 0, 'Java,试卷,考试', 'approved', '2025-12-04 23:05:20');
INSERT INTO `tb_resource` VALUES (7, 1, 'Java程序设计', 'Java程序设计B卷.docx', 'document', 'Java程序设计期末试卷B卷', 'http://120.26.212.210/resources/testpaper/Java程序设计B卷.docx', 'http://120.26.212.210/resources/testpaper/Java程序设计B卷.pdf', 158000, 1, '张教授', 0, 'Java,试卷,考试', 'approved', '2025-12-04 23:05:20');
INSERT INTO `tb_resource` VALUES (8, 2, 'Web开发技术', 'Web开发技术试卷.docx', 'document', 'Web开发技术期末试卷', 'http://120.26.212.210/resources/testpaper/Web开发技术试卷.docx', 'http://120.26.212.210/resources/testpaper/Web开发技术试卷.pdf', 142000, 3, '王讲师', 0, 'Web,试卷,考试', 'approved', '2025-12-04 23:05:20');
INSERT INTO `tb_resource` VALUES (9, 4, '数据结构', '数据结构试卷A.docx', 'document', '数据结构期末试卷A卷', 'http://120.26.212.210/resources/testpaper/数据结构试卷A.docx', 'http://120.26.212.210/resources/testpaper/数据结构试卷A.pdf', 168000, 1, '张教授', 0, '数据结构,试卷,考试', 'approved', '2025-12-04 23:05:20');
INSERT INTO `tb_resource` VALUES (10, 4, '数据结构', '数据结构试卷B.docx', 'document', '数据结构期末试卷B卷', 'http://120.26.212.210/resources/testpaper/数据结构试卷B.docx', 'http://120.26.212.210/resources/testpaper/数据结构试卷B.pdf', 165000, 1, '张教授', 0, '数据结构,试卷,考试', 'approved', '2025-12-04 23:05:20');
INSERT INTO `tb_resource` VALUES (11, 4, '数据结构', '数据结构试卷C.docx', 'document', '数据结构期末试卷C卷', 'http://120.26.212.210/resources/testpaper/数据结构试卷C.docx', 'http://120.26.212.210/resources/testpaper/数据结构试卷C.pdf', 170000, 1, '张教授', 0, '数据结构,试卷,考试', 'approved', '2025-12-04 23:05:20');
INSERT INTO `tb_resource` VALUES (12, 1, 'Java程序设计', 'Java程序设计视频.mp4', 'video', 'Java程序设计视频教程', 'http://120.26.212.210/resources/videos/Java程序设计视频.mp4', NULL, 528000000, 1, '张教授', 0, 'Java,视频,教程', 'approved', '2025-12-04 23:05:20');
INSERT INTO `tb_resource` VALUES (13, 2, 'Web开发技术', 'web开发技术视频.mp4', 'video', 'Web开发技术视频教程', 'http://120.26.212.210/resources/videos/web开发技术视频.mp4', NULL, 456000000, 3, '王讲师', 0, 'Web,视频,教程', 'approved', '2025-12-04 23:05:20');
INSERT INTO `tb_resource` VALUES (14, 3, '操作系统', '操纵系统视频.mp4', 'video', '操作系统原理视频教程', 'http://120.26.212.210/resources/videos/操纵系统视频.mp4', NULL, 612000000, 1, '张教授', 0, '操作系统,视频,教程', 'approved', '2025-12-04 23:05:20');
INSERT INTO `tb_resource` VALUES (15, 4, '数据结构', '数据结构视频.mp4', 'video', '数据结构与算法视频教程', 'http://120.26.212.210/resources/videos/数据结构视频.mp4', NULL, 589000000, 1, '张教授', 0, '数据结构,视频,教程', 'approved', '2025-12-04 23:05:20');
INSERT INTO `tb_resource` VALUES (16, 5, '数据库原理', '数据库原理视频.mp4', 'video', '数据库原理与应用视频教程', 'http://120.26.212.210/resources/videos/数据库原理视频.mp4', NULL, 498000000, 2, '李老师', 0, '数据库,视频,教程', 'approved', '2025-12-04 23:05:20');

-- ----------------------------
-- Table structure for tb_schedule
-- ----------------------------
DROP TABLE IF EXISTS `tb_schedule`;
CREATE TABLE `tb_schedule`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '排课ID',
  `course_id` bigint NOT NULL COMMENT '课程ID',
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名称(冗余)',
  `teacher_id` bigint NOT NULL COMMENT '教师ID',
  `teacher_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '教师姓名(冗余)',
  `classroom` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '教室',
  `day_of_week` tinyint NOT NULL COMMENT '星期(1-7)',
  `period` tinyint NOT NULL COMMENT '节次(1-5)',
  `start_week` tinyint NOT NULL COMMENT '起始周',
  `end_week` tinyint NOT NULL COMMENT '结束周',
  `semester` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学期',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_schedule`(`classroom` ASC, `day_of_week` ASC, `period` ASC, `semester` ASC) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_semester`(`semester` ASC) USING BTREE,
  INDEX `idx_day_period`(`day_of_week` ASC, `period` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '课程表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_schedule
-- ----------------------------
INSERT INTO `tb_schedule` VALUES (10, 1, 'Java程序设计', 3, '张教授', 'A101', 2, 1, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (11, 1, 'Java程序设计', 3, '张教授', 'A101', 3, 2, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (12, 2, 'Web开发技术', 5, '王讲师', 'B201', 2, 2, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (13, 2, 'Web开发技术', 5, '王讲师', 'B201', 4, 1, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (14, 3, '操作系统', 3, '张教授', 'A102', 1, 2, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (15, 3, '操作系统', 3, '张教授', 'A102', 3, 1, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (16, 4, '数据结构', 3, '张教授', 'A103', 2, 1, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (17, 4, '数据结构', 3, '张教授', 'A103', 4, 2, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (18, 5, '数据库原理', 4, '李老师', 'B202', 3, 3, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (19, 5, '数据库原理', 4, '李老师', 'B202', 5, 2, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (20, 6, '计算机网络', 5, '王讲师', 'B203', 1, 3, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (21, 6, '计算机网络', 5, '王讲师', 'B203', 4, 3, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (22, 7, '软件工程', 3, '张教授', 'A104', 2, 3, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (23, 7, '软件工程', 3, '张教授', 'A104', 5, 1, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (24, 8, '人工智能导论', 4, '李老师', 'C301', 3, 4, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (25, 9, '移动应用开发', 5, '王讲师', 'C302', 4, 4, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (26, 10, '大数据技术', 4, '李老师', 'B204', 5, 3, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (27, 11, '云计算技术', 5, '王讲师', 'C303', 5, 4, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (28, 12, '信息安全', 3, '张教授', 'B205', 2, 4, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (29, 13, '高等数学A', 3, '张教授', 'D401', 1, 5, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (30, 13, '高等数学A', 3, '张教授', 'D401', 3, 5, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (31, 13, '高等数学A', 3, '张教授', 'D401', 5, 5, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (32, 14, '大学英语', 4, '李老师', 'D401', 1, 4, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (33, 14, '大学英语', 4, '李老师', 'D402', 4, 5, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (34, 15, '体育', 5, '王讲师', '体育馆', 3, 3, 1, 16, '2025-2026-1');
INSERT INTO `tb_schedule` VALUES (35, 15, '体育', 5, '王讲师', '操场', 5, 3, 1, 16, '2025-2026-1');

-- ----------------------------
-- Table structure for tb_student
-- ----------------------------
DROP TABLE IF EXISTS `tb_student`;
CREATE TABLE `tb_student`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '学生ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `student_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学号',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专业',
  `class_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '班级',
  `enrollment_year` int NULL DEFAULT NULL COMMENT '入学年份',
  `grade` int NULL DEFAULT NULL COMMENT '年级',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_student_number`(`student_number` ASC) USING BTREE,
  INDEX `idx_major_grade`(`major` ASC, `grade` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_student
-- ----------------------------
INSERT INTO `tb_student` VALUES (1, 6, '2023001', '计算机科学与技术', '计算机2101', 2022, 4);
INSERT INTO `tb_student` VALUES (2, 7, '2023002', '计算机科学与技术', '计算机2101', 2021, 3);
INSERT INTO `tb_student` VALUES (3, 8, '2023003', '软件工程', '软件2101', 2021, 4);
INSERT INTO `tb_student` VALUES (4, 9, '2023005', '计算机科学与技术', '计算机2201', 2022, 3);
INSERT INTO `tb_student` VALUES (5, 10, '2023006', '软件工程', '软件2201', 2022, 3);
INSERT INTO `tb_student` VALUES (6, 13, '2023007', '计算机科学', '计科2101', 2025, 1);

-- ----------------------------
-- Table structure for tb_student_answer
-- ----------------------------
DROP TABLE IF EXISTS `tb_student_answer`;
CREATE TABLE `tb_student_answer`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '答题记录ID',
  `student_exam_id` bigint NOT NULL COMMENT '学生考试记录ID',
  `attempt_number` int NOT NULL DEFAULT 1 COMMENT '尝试次数',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `question_order` int NOT NULL COMMENT '题目序号',
  `student_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '学生答案',
  `is_correct` tinyint(1) NULL DEFAULT NULL COMMENT '是否正确(客观题)',
  `score` int NULL DEFAULT NULL COMMENT '得分',
  `answer_time` datetime NULL DEFAULT NULL COMMENT '答题时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_exam_id`(`student_exam_id` ASC) USING BTREE,
  INDEX `idx_question_id`(`question_id` ASC) USING BTREE,
  INDEX `idx_exam_order`(`student_exam_id` ASC, `question_order` ASC) USING BTREE,
  INDEX `idx_exam_question_attempt`(`student_exam_id` ASC, `question_id` ASC, `attempt_number` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 73 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生答题详情表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_student_answer
-- ----------------------------
INSERT INTO `tb_student_answer` VALUES (1, 3, 1, 1, 1, 'float', 0, 0, '2025-12-02 22:59:52');
INSERT INTO `tb_student_answer` VALUES (2, 3, 1, 2, 2, 'class', 0, 0, '2025-12-02 22:59:53');
INSERT INTO `tb_student_answer` VALUES (3, 3, 1, 5, 3, 'public', 0, 0, '2025-12-02 22:59:54');
INSERT INTO `tb_student_answer` VALUES (4, 3, 1, 6, 4, 'ArrayList', 0, 0, '2025-12-02 22:59:55');
INSERT INTO `tb_student_answer` VALUES (5, 3, 1, 9, 5, '对', 0, 0, '2025-12-02 22:59:57');
INSERT INTO `tb_student_answer` VALUES (6, 3, 1, 10, 6, '对', 0, 0, '2025-12-02 22:59:59');
INSERT INTO `tb_student_answer` VALUES (7, 3, 1, 13, 7, '啊啊a', 0, 0, '2025-12-02 23:00:00');
INSERT INTO `tb_student_answer` VALUES (8, 3, 1, 14, 8, '啊\n', 0, 0, '2025-12-02 23:00:01');
INSERT INTO `tb_student_answer` VALUES (9, 3, 1, 17, 9, 'a ', 0, 0, '2025-12-02 23:00:02');
INSERT INTO `tb_student_answer` VALUES (10, 3, 1, 21, 10, '啊\n', 0, 0, '2025-12-02 23:00:04');
INSERT INTO `tb_student_answer` VALUES (12, 4, 1, 8, 1, 'B', 1, 5, '2025-12-03 13:39:18');
INSERT INTO `tb_student_answer` VALUES (13, 4, 1, 9, 2, 'B', 0, 0, '2025-12-03 13:39:18');
INSERT INTO `tb_student_answer` VALUES (14, 4, 1, 10, 3, 'B', 0, 0, '2025-12-03 13:39:18');
INSERT INTO `tb_student_answer` VALUES (15, 4, 1, 11, 4, 'B', 0, 0, '2025-12-03 13:39:19');
INSERT INTO `tb_student_answer` VALUES (16, 4, 1, 12, 5, 'B', 0, 0, '2025-12-03 13:39:19');
INSERT INTO `tb_student_answer` VALUES (18, 15, 2, 8, 1, 'B', 1, 5, '2025-12-03 14:27:53');
INSERT INTO `tb_student_answer` VALUES (19, 15, 2, 9, 2, 'C', 1, 5, '2025-12-03 14:27:53');
INSERT INTO `tb_student_answer` VALUES (20, 15, 2, 10, 3, 'B', 0, 0, '2025-12-03 14:27:53');
INSERT INTO `tb_student_answer` VALUES (21, 15, 2, 11, 4, 'B', 0, 0, '2025-12-03 14:27:53');
INSERT INTO `tb_student_answer` VALUES (22, 15, 2, 12, 5, 'B', 0, 0, '2025-12-03 14:27:53');
INSERT INTO `tb_student_answer` VALUES (23, 17, 4, 8, 1, 'B', 1, 5, '2025-12-03 15:03:59');
INSERT INTO `tb_student_answer` VALUES (24, 17, 4, 9, 2, 'B', 0, 0, '2025-12-03 15:03:59');
INSERT INTO `tb_student_answer` VALUES (25, 17, 4, 10, 3, 'B', 0, 0, '2025-12-03 15:03:59');
INSERT INTO `tb_student_answer` VALUES (26, 17, 4, 11, 4, 'B', 0, 0, '2025-12-03 15:03:59');
INSERT INTO `tb_student_answer` VALUES (27, 17, 4, 12, 5, 'A', 0, 0, '2025-12-03 15:03:59');
INSERT INTO `tb_student_answer` VALUES (28, 18, 4, 8, 1, 'B', 1, 5, '2025-12-03 15:17:17');
INSERT INTO `tb_student_answer` VALUES (29, 18, 4, 9, 2, 'D', 0, 0, '2025-12-03 15:17:17');
INSERT INTO `tb_student_answer` VALUES (30, 18, 4, 10, 3, 'D', 0, 0, '2025-12-03 15:17:17');
INSERT INTO `tb_student_answer` VALUES (31, 18, 4, 11, 4, 'B', 0, 0, '2025-12-03 15:17:17');
INSERT INTO `tb_student_answer` VALUES (32, 18, 4, 12, 5, 'A', 0, 0, '2025-12-03 15:17:17');
INSERT INTO `tb_student_answer` VALUES (33, 19, 5, 8, 1, 'A', 0, 0, '2025-12-03 17:04:36');
INSERT INTO `tb_student_answer` VALUES (34, 19, 5, 9, 2, 'A', 0, 0, '2025-12-03 17:04:36');
INSERT INTO `tb_student_answer` VALUES (35, 19, 5, 10, 3, 'A', 0, 0, '2025-12-03 17:04:36');
INSERT INTO `tb_student_answer` VALUES (36, 19, 5, 11, 4, 'A', 0, 0, '2025-12-03 17:04:36');
INSERT INTO `tb_student_answer` VALUES (37, 19, 5, 12, 5, 'A', 0, 0, '2025-12-03 17:04:37');
INSERT INTO `tb_student_answer` VALUES (38, 28, 14, 8, 1, '', 0, 0, '2025-12-03 17:37:33');
INSERT INTO `tb_student_answer` VALUES (39, 28, 14, 9, 2, '', 0, 0, '2025-12-03 17:37:34');
INSERT INTO `tb_student_answer` VALUES (40, 28, 14, 10, 3, '', 0, 0, '2025-12-03 17:37:35');
INSERT INTO `tb_student_answer` VALUES (41, 28, 14, 11, 4, '', 0, 0, '2025-12-03 17:37:35');
INSERT INTO `tb_student_answer` VALUES (42, 28, 14, 12, 5, '', 0, 0, '2025-12-03 17:37:36');
INSERT INTO `tb_student_answer` VALUES (43, 28, 14, 8, 1, '', 0, 0, '2025-12-03 17:37:46');
INSERT INTO `tb_student_answer` VALUES (44, 28, 14, 9, 2, '', 0, 0, '2025-12-03 17:37:47');
INSERT INTO `tb_student_answer` VALUES (45, 28, 14, 10, 3, '', 0, 0, '2025-12-03 17:37:47');
INSERT INTO `tb_student_answer` VALUES (46, 28, 14, 11, 4, '', 0, 0, '2025-12-03 17:37:48');
INSERT INTO `tb_student_answer` VALUES (47, 28, 14, 12, 5, '', 0, 0, '2025-12-03 17:37:49');
INSERT INTO `tb_student_answer` VALUES (48, 29, 15, 8, 1, '', 0, 0, '2025-12-03 17:38:38');
INSERT INTO `tb_student_answer` VALUES (49, 29, 15, 9, 2, '', 0, 0, '2025-12-03 17:38:39');
INSERT INTO `tb_student_answer` VALUES (50, 29, 15, 10, 3, '', 0, 0, '2025-12-03 17:38:39');
INSERT INTO `tb_student_answer` VALUES (51, 29, 15, 11, 4, '', 0, 0, '2025-12-03 17:38:40');
INSERT INTO `tb_student_answer` VALUES (52, 29, 15, 12, 5, '', 0, 0, '2025-12-03 17:38:41');
INSERT INTO `tb_student_answer` VALUES (53, 30, 8, 8, 1, 'A', 0, 0, '2025-12-05 01:03:08');
INSERT INTO `tb_student_answer` VALUES (54, 30, 8, 9, 2, 'B', 0, 0, '2025-12-05 01:03:08');
INSERT INTO `tb_student_answer` VALUES (55, 30, 8, 10, 3, 'C', 1, 5, '2025-12-05 01:03:08');
INSERT INTO `tb_student_answer` VALUES (56, 30, 8, 11, 4, 'B', 0, 0, '2025-12-05 01:03:08');
INSERT INTO `tb_student_answer` VALUES (57, 30, 8, 12, 5, 'C', 1, 5, '2025-12-05 01:03:08');
INSERT INTO `tb_student_answer` VALUES (58, 31, 9, 8, 1, 'B', 1, 5, '2025-12-05 01:10:32');
INSERT INTO `tb_student_answer` VALUES (59, 31, 9, 9, 2, 'A', 0, 0, '2025-12-05 01:10:32');
INSERT INTO `tb_student_answer` VALUES (60, 31, 9, 10, 3, 'A', 0, 0, '2025-12-05 01:10:33');
INSERT INTO `tb_student_answer` VALUES (61, 31, 9, 11, 4, 'A', 0, 0, '2025-12-05 01:10:33');
INSERT INTO `tb_student_answer` VALUES (62, 31, 9, 12, 5, 'B', 0, 0, '2025-12-05 01:10:33');
INSERT INTO `tb_student_answer` VALUES (63, 34, 12, 8, 1, 'B', 1, 5, '2025-12-09 19:49:58');
INSERT INTO `tb_student_answer` VALUES (64, 34, 12, 9, 2, 'B', 0, 0, '2025-12-09 19:49:58');
INSERT INTO `tb_student_answer` VALUES (65, 34, 12, 10, 3, 'B', 0, 0, '2025-12-09 19:49:58');
INSERT INTO `tb_student_answer` VALUES (66, 34, 12, 11, 4, 'B', 0, 0, '2025-12-09 19:49:58');
INSERT INTO `tb_student_answer` VALUES (67, 34, 12, 12, 5, 'B', 0, 0, '2025-12-09 19:49:58');
INSERT INTO `tb_student_answer` VALUES (68, 35, 13, 8, 1, 'B', 1, 5, '2025-12-16 22:33:08');
INSERT INTO `tb_student_answer` VALUES (69, 35, 13, 9, 2, 'B', 0, 0, '2025-12-16 22:33:08');
INSERT INTO `tb_student_answer` VALUES (70, 35, 13, 10, 3, 'B', 0, 0, '2025-12-16 22:33:08');
INSERT INTO `tb_student_answer` VALUES (71, 35, 13, 11, 4, 'B', 0, 0, '2025-12-16 22:33:08');
INSERT INTO `tb_student_answer` VALUES (72, 35, 13, 12, 5, 'B', 0, 0, '2025-12-16 22:33:08');

-- ----------------------------
-- Table structure for tb_student_exam
-- ----------------------------
DROP TABLE IF EXISTS `tb_student_exam`;
CREATE TABLE `tb_student_exam`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '考试记录ID',
  `exam_paper_id` bigint NOT NULL COMMENT '试卷ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学生姓名(冗余)',
  `student_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学号(冗余)',
  `attempt_number` int NOT NULL DEFAULT 1 COMMENT '尝试次数',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始答题时间',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `total_score` int NULL DEFAULT NULL COMMENT '总得分',
  `objective_score` int NULL DEFAULT NULL COMMENT '客观题得分(自动批改)',
  `subjective_score` int NULL DEFAULT NULL COMMENT '主观题得分(教师批改)',
  `status` enum('not_started','in_progress','submitted','graded') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'not_started' COMMENT '考试状态',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `answer_sheet` json NULL COMMENT '答题卡(JSON对象)',
  `grader_id` bigint NULL DEFAULT NULL COMMENT '批改者ID',
  `grader_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '批改者姓名',
  `grade_time` datetime NULL DEFAULT NULL COMMENT '批改时间',
  `feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评语',
  `duration` int NULL DEFAULT NULL COMMENT '考试时长(分钟)',
  `pass_score` int NULL DEFAULT NULL COMMENT '及格分数',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_exam_status`(`exam_paper_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_student_status`(`student_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_submit_time`(`submit_time` DESC) USING BTREE,
  INDEX `idx_exam_student_attempt`(`exam_paper_id` ASC, `student_id` ASC, `attempt_number` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生考试记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_student_exam
-- ----------------------------
INSERT INTO `tb_student_exam` VALUES (3, 1, 6, '张三', 'student001', 1, '2025-12-02 22:48:55', '2025-12-02 23:00:22', 0, 0, 0, 'submitted', '0:0:0:0:0:0:0:1', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `tb_student_exam` VALUES (4, 3, 6, '张三', 'student001', 1, '2025-12-03 00:58:20', '2025-12-03 14:24:49', 5, 5, 0, 'submitted', '0:0:0:0:0:0:0:1', NULL, NULL, NULL, NULL, NULL, 90, 60);
INSERT INTO `tb_student_exam` VALUES (15, 3, 6, '张三', 'student001', 2, '2025-12-03 14:27:44', '2025-12-03 14:27:54', 10, 10, 0, 'submitted', '0:0:0:0:0:0:0:1', NULL, NULL, NULL, NULL, NULL, 90, 60);
INSERT INTO `tb_student_exam` VALUES (17, 3, 6, '张三', 'student001', 4, '2025-12-03 15:03:52', '2025-12-03 15:03:59', 5, 5, 0, 'submitted', '0:0:0:0:0:0:0:1', NULL, NULL, NULL, NULL, NULL, 90, 60);
INSERT INTO `tb_student_exam` VALUES (18, 3, 6, '张三', 'student001', 4, '2025-12-03 15:17:05', '2025-12-03 15:17:17', 5, 5, 0, 'submitted', '0:0:0:0:0:0:0:1', NULL, NULL, NULL, NULL, NULL, 90, 60);
INSERT INTO `tb_student_exam` VALUES (19, 3, 6, '张三', 'student001', 5, '2025-12-03 17:04:29', '2025-12-03 17:04:37', 0, 0, 0, 'submitted', '0:0:0:0:0:0:0:1', NULL, NULL, NULL, NULL, NULL, 90, 60);
INSERT INTO `tb_student_exam` VALUES (28, 3, 6, '张三', 'student001', 14, '2025-12-03 17:37:09', '2025-12-03 17:37:58', 0, 0, 0, 'submitted', '0:0:0:0:0:0:0:1', NULL, NULL, NULL, NULL, NULL, 90, 60);
INSERT INTO `tb_student_exam` VALUES (29, 3, 6, '张三', 'student001', 15, '2025-12-03 17:38:22', '2025-12-03 17:38:51', 0, 0, 0, 'submitted', '0:0:0:0:0:0:0:1', NULL, NULL, NULL, NULL, NULL, 90, 60);
INSERT INTO `tb_student_exam` VALUES (30, 3, 6, '张三', 'student001', 8, '2025-12-05 01:02:57', '2025-12-05 01:03:08', 10, 10, 0, 'submitted', '127.0.0.1', NULL, NULL, NULL, NULL, NULL, 90, 60);
INSERT INTO `tb_student_exam` VALUES (31, 3, 6, '张三', 'student001', 9, '2025-12-05 01:10:19', '2025-12-05 01:10:34', 5, 5, 0, 'submitted', '0:0:0:0:0:0:0:1', NULL, NULL, NULL, NULL, NULL, 90, 60);
INSERT INTO `tb_student_exam` VALUES (32, 3, 6, '张三', 'student001', 10, '2025-12-05 14:03:59', NULL, NULL, NULL, NULL, 'in_progress', '0:0:0:0:0:0:0:1', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `tb_student_exam` VALUES (33, 3, 6, '张三', 'student001', 11, '2025-12-05 14:04:39', NULL, NULL, NULL, NULL, 'in_progress', '0:0:0:0:0:0:0:1', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `tb_student_exam` VALUES (34, 3, 6, '张三', 'student001', 12, '2025-12-09 19:49:47', '2025-12-09 19:49:59', 5, 5, 0, 'submitted', '0:0:0:0:0:0:0:1', NULL, NULL, NULL, NULL, NULL, 90, 60);
INSERT INTO `tb_student_exam` VALUES (35, 3, 6, '张三', 'student001', 13, '2025-12-16 22:32:58', '2025-12-16 22:33:10', 5, 5, 0, 'submitted', '0:0:0:0:0:0:0:1', NULL, NULL, NULL, NULL, NULL, 90, 60);

-- ----------------------------
-- Table structure for tb_student_homework
-- ----------------------------
DROP TABLE IF EXISTS `tb_student_homework`;
CREATE TABLE `tb_student_homework`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '作业记录ID',
  `homework_id` bigint NOT NULL COMMENT '作业ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学生姓名(冗余)',
  `student_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学号(冗余)',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `score` decimal(5, 2) NULL DEFAULT NULL COMMENT '得分',
  `status` enum('not_started','in_progress','submitted','graded') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'not_started' COMMENT '作业状态',
  `is_late` tinyint(1) NULL DEFAULT 0 COMMENT '是否迟交',
  `teacher_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '教师评语',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_homework_student`(`homework_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生作业记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_student_homework
-- ----------------------------
INSERT INTO `tb_student_homework` VALUES (14, 12, 6, '张三', 'student001', '2025-12-05 01:40:03', '2025-12-05 01:40:29', 15.00, 'submitted', 0, NULL, '2025-12-05 01:40:05', '2025-12-05 01:40:31');
INSERT INTO `tb_student_homework` VALUES (15, 13, 6, '张三', 'student001', '2025-12-05 15:10:40', NULL, NULL, 'in_progress', NULL, NULL, '2025-12-05 15:10:40', '2025-12-05 15:10:40');

-- ----------------------------
-- Table structure for tb_student_homework_answer
-- ----------------------------
DROP TABLE IF EXISTS `tb_student_homework_answer`;
CREATE TABLE `tb_student_homework_answer`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '答题记录ID',
  `student_homework_id` bigint NOT NULL COMMENT '学生作业记录ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `student_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '学生答案',
  `is_correct` tinyint(1) NULL DEFAULT NULL COMMENT '是否正确(客观题自动判断)',
  `score` decimal(5, 2) NULL DEFAULT NULL COMMENT '得分',
  `teacher_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '教师批注',
  `answer_time` datetime NULL DEFAULT NULL COMMENT '答题时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_homework_question`(`student_homework_id` ASC, `question_id` ASC) USING BTREE,
  INDEX `idx_student_homework_id`(`student_homework_id` ASC) USING BTREE,
  INDEX `idx_question_id`(`question_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生作业答题记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_student_homework_answer
-- ----------------------------
INSERT INTO `tb_student_homework_answer` VALUES (25, 14, 46, 'class', 1, 10.00, NULL, NULL);
INSERT INTO `tb_student_homework_answer` VALUES (26, 14, 32, 'false', 1, 5.00, NULL, NULL);
INSERT INTO `tb_student_homework_answer` VALUES (27, 14, 33, 'false', 0, 0.00, NULL, NULL);
INSERT INTO `tb_student_homework_answer` VALUES (28, 14, 58, '11', 0, 0.00, NULL, NULL);
INSERT INTO `tb_student_homework_answer` VALUES (29, 14, 59, 'ww', 0, 0.00, NULL, NULL);
INSERT INTO `tb_student_homework_answer` VALUES (30, 14, 23, 'A,B,C,D', 0, 0.00, NULL, NULL);
INSERT INTO `tb_student_homework_answer` VALUES (31, 14, 25, 'A,B', 0, 0.00, NULL, NULL);
INSERT INTO `tb_student_homework_answer` VALUES (32, 14, 26, 'A,B', 0, 0.00, NULL, NULL);

-- ----------------------------
-- Table structure for tb_student_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `tb_student_operation_log`;
CREATE TABLE `tb_student_operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学生姓名',
  `student_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学号',
  `operation` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作模块(course/homework/exam/resource/discussion)',
  `target_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标类型(course/assignment/question/file)',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标ID',
  `target_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求方法(GET/POST/PUT/DELETE)',
  `request_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求URL',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数(JSON格式)',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户代理(浏览器信息)',
  `status` enum('success','failed') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'success' COMMENT '操作状态',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `execution_time` int NULL DEFAULT NULL COMMENT '执行时长(毫秒)',
  `operation_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_student_number`(`student_number` ASC) USING BTREE,
  INDEX `idx_operation_time`(`operation_time` ASC) USING BTREE,
  INDEX `idx_module`(`module` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_student_operation_log
-- ----------------------------

-- ----------------------------
-- Table structure for tb_teacher
-- ----------------------------
DROP TABLE IF EXISTS `tb_teacher`;
CREATE TABLE `tb_teacher`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '教师ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `teacher_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工号',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属院系',
  `title` enum('assistant','lecturer','associate_professor','professor') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '职称',
  `research_field` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '研究方向',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_teacher_number`(`teacher_number` ASC) USING BTREE,
  INDEX `idx_department`(`department` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '教师表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_teacher
-- ----------------------------
INSERT INTO `tb_teacher` VALUES (1, 3, 'T001', '计算机学院', 'professor', 'Java程序设计,软件工程');
INSERT INTO `tb_teacher` VALUES (2, 4, 'T002', '计算学院', 'professor', '数据库原理,数据');
INSERT INTO `tb_teacher` VALUES (3, 5, 'T003', '计算机学院', 'associate_professor', 'Web开发,云计算');
INSERT INTO `tb_teacher` VALUES (4, 11, '1234', '机械', 'associate_professor', '不知道');

-- ----------------------------
-- Table structure for tb_teacher_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `tb_teacher_operation_log`;
CREATE TABLE `tb_teacher_operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `teacher_id` bigint NOT NULL COMMENT '教师ID',
  `teacher_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '教师姓名',
  `teacher_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '工号',
  `operation` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作模块(course/homework/exam/resource/grading)',
  `target_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标类型(course/assignment/exam/student)',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标ID',
  `target_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求方法(GET/POST/PUT/DELETE)',
  `request_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求URL',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数(JSON格式)',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户代理(浏览器信息)',
  `status` enum('success','failed') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'success' COMMENT '操作状态',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '错误信息',
  `execution_time` int NULL DEFAULT NULL COMMENT '执行时长(毫秒)',
  `operation_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_teacher_number`(`teacher_number` ASC) USING BTREE,
  INDEX `idx_operation_time`(`operation_time` ASC) USING BTREE,
  INDEX `idx_module`(`module` ASC) USING BTREE,
  INDEX `idx_target`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '教师操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_teacher_operation_log
-- ----------------------------

-- ----------------------------
-- Table structure for tb_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(BCrypt加密)',
  `role` enum('student','teacher','admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户角色',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `gender` enum('male','female') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'male' COMMENT '性别',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `status` enum('active','inactive') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active' COMMENT '账户状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  INDEX `idx_role_status`(`role` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tb_user
-- ----------------------------
INSERT INTO `tb_user` VALUES (1, 'admin001', '$2a$10$fDBl12LsI3mibADOoxspROhoGVVPMjzOaJ.Bjzki.e3nRueh5FHZK', 'admin', '系统管理', 'male', 'asikeida@gmail.com', '19858150253', NULL, 'active', '2025-11-26 22:33:04', '2025-12-16 22:40:01');
INSERT INTO `tb_user` VALUES (2, 'admin002', '$2a$10$fDBl12LsI3mibADOoxspROhoGVVPMjzOaJ.Bjzki.e3nRueh5FHZK', 'admin', '王管理', 'female', 'admin2@example.com', '13800000002', NULL, 'active', '2025-11-26 22:33:04', '2025-12-16 22:40:01');
INSERT INTO `tb_user` VALUES (3, 'teacher001', '$2a$10$fDBl12LsI3mibADOoxspROhoGVVPMjzOaJ.Bjzki.e3nRueh5FHZK', 'teacher', '张教授', 'male', 'teacher001@example.com', '13800001001', NULL, 'active', '2025-11-26 22:33:04', '2025-12-16 22:40:01');
INSERT INTO `tb_user` VALUES (4, 'teacher002', '$2a$10$fDBl12LsI3mibADOoxspROhoGVVPMjzOaJ.Bjzki.e3nRueh5FHZK', 'teacher', '李老', 'male', 'eacher002@example.com', '13800001002', NULL, 'active', '2025-11-26 22:33:04', '2025-12-16 22:40:01');
INSERT INTO `tb_user` VALUES (5, 'teacher003', '$2a$10$fDBl12LsI3mibADOoxspROhoGVVPMjzOaJ.Bjzki.e3nRueh5FHZK', 'teacher', '王讲师', 'male', 'teacher003@example.com', '13800001003', NULL, 'active', '2025-11-26 22:33:04', '2025-12-16 22:40:01');
INSERT INTO `tb_user` VALUES (6, 'student001', '$2a$10$fDBl12LsI3mibADOoxspROhoGVVPMjzOaJ.Bjzki.e3nRueh5FHZK', 'student', '张三', 'male', '3150503593@qq.com', '13900001001', NULL, 'active', '2025-11-26 22:33:04', '2025-12-16 22:16:39');
INSERT INTO `tb_user` VALUES (7, 'student002', '$2a$10$fDBl12LsI3mibADOoxspROhoGVVPMjzOaJ.Bjzki.e3nRueh5FHZK', 'student', '李四', 'male', '2753132572@qq.com', '13900001002', NULL, 'active', '2025-11-26 22:33:04', '2025-12-17 14:22:26');
INSERT INTO `tb_user` VALUES (8, 'student003', '$2a$10$fDBl12LsI3mibADOoxspROhoGVVPMjzOaJ.Bjzki.e3nRueh5FHZK', 'student', '王五', 'female', '3080259334@qq.com', '13900001003', NULL, 'active', '2025-11-26 22:33:04', '2025-12-23 19:30:04');
INSERT INTO `tb_user` VALUES (9, 'student004', '$2a$10$fDBl12LsI3mibADOoxspROhoGVVPMjzOaJ.Bjzki.e3nRueh5FHZK', 'student', '赵六', 'male', 'student004@example.com', '13900001004', NULL, 'active', '2025-11-26 22:33:04', '2025-12-16 22:40:01');
INSERT INTO `tb_user` VALUES (10, 'student005', '$2a$10$fDBl12LsI3mibADOoxspROhoGVVPMjzOaJ.Bjzki.e3nRueh5FHZK', 'student', '孙七', 'female', 'student005@example.com', '13900001005', NULL, 'active', '2025-11-26 22:33:04', '2025-12-16 22:40:01');
INSERT INTO `tb_user` VALUES (11, 'test', '$2a$10$fDBl12LsI3mibADOoxspROhoGVVPMjzOaJ.Bjzki.e3nRueh5FHZK', 'teacher', 'test', 'male', 'adfadf@gmail.com', '19858123423', NULL, 'active', '2025-12-04 17:59:04', '2025-12-16 22:40:01');
INSERT INTO `tb_user` VALUES (13, '2012312', '$2a$10$fDBl12LsI3mibADOoxspROhoGVVPMjzOaJ.Bjzki.e3nRueh5FHZK', 'student', '七七', 'male', 'fasdf001@example.com', '13800138000', NULL, 'active', '2025-12-04 18:58:16', '2025-12-16 22:40:01');
INSERT INTO `tb_user` VALUES (15, 'admin003', '$2a$10$fDBl12LsI3mibADOoxspROhoGVVPMjzOaJ.Bjzki.e3nRueh5FHZK', 'admin', '张管理', 'male', 'admin3@example.com', '13800000003', NULL, 'active', '2025-12-23 18:56:09', '2025-12-23 19:14:57');
INSERT INTO `tb_user` VALUES (16, 'admin004', '$2a$10$fDBl12LsI3mibADOoxspROhoGVVPMjzOaJ.Bjzki.e3nRueh5FHZK', 'admin', '李管理', 'female', 'admin4@example.com', '13800000004', NULL, 'active', '2025-12-23 18:56:09', '2025-12-23 19:15:11');

-- ----------------------------
-- View structure for v_admin_permissions
-- ----------------------------
DROP VIEW IF EXISTS `v_admin_permissions`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_admin_permissions` AS select `a`.`id` AS `admin_id`,`a`.`user_id` AS `user_id`,`u`.`username` AS `username`,`u`.`real_name` AS `real_name`,`a`.`admin_number` AS `admin_number`,`a`.`permission_level` AS `permission_level`,`a`.`role_name` AS `role_name`,`a`.`department` AS `department`,`a`.`last_login_time` AS `last_login_time`,`r`.`permissions` AS `role_permissions`,`u`.`status` AS `status` from ((`tb_admin` `a` join `tb_user` `u` on((`a`.`user_id` = `u`.`id`))) left join `tb_admin_role` `r` on((`a`.`permission_level` = `r`.`permission_level`))) where (`u`.`role` = 'admin');

-- ----------------------------
-- View structure for v_course_detail
-- ----------------------------
DROP VIEW IF EXISTS `v_course_detail`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_course_detail` AS select `c`.`id` AS `id`,`c`.`course_code` AS `course_code`,`c`.`name` AS `name`,`c`.`description` AS `description`,`c`.`semester` AS `semester`,`c`.`credit` AS `credit`,`c`.`capacity` AS `capacity`,`c`.`enrolled` AS `enrolled`,`c`.`category` AS `category`,`c`.`status` AS `status`,`c`.`teacher_id` AS `teacher_id`,`c`.`teacher_name` AS `teacher_name`,`t`.`department` AS `teacher_department`,`t`.`title` AS `teacher_title`,`t`.`user_id` AS `teacher_user_id`,`c`.`create_time` AS `create_time`,`c`.`update_time` AS `update_time` from (`tb_course` `c` left join `tb_teacher` `t` on((`c`.`teacher_id` = `t`.`id`)));

-- ----------------------------
-- View structure for v_course_discussion_activity
-- ----------------------------
DROP VIEW IF EXISTS `v_course_discussion_activity`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_course_discussion_activity` AS select `d`.`course_id` AS `course_id`,`d`.`course_name` AS `course_name`,count(`d`.`id`) AS `total_topics`,sum(`d`.`view_count`) AS `total_views`,sum(`d`.`reply_count`) AS `total_replies`,count(distinct `d`.`author_id`) AS `active_users`,sum((case when (`d`.`topic_type` = 'question') then 1 else 0 end)) AS `question_count`,sum((case when (`d`.`is_resolved` = 1) then 1 else 0 end)) AS `resolved_count`,max(`d`.`create_time`) AS `last_topic_time` from `tb_discussion` `d` where (`d`.`status` = 'active') group by `d`.`course_id`,`d`.`course_name`;

-- ----------------------------
-- View structure for v_course_resource_statistics
-- ----------------------------
DROP VIEW IF EXISTS `v_course_resource_statistics`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_course_resource_statistics` AS select `c`.`id` AS `course_id`,`c`.`name` AS `course_name`,`c`.`teacher_name` AS `teacher_name`,count(`r`.`id`) AS `resource_count`,sum((case when (`r`.`type` = 'document') then 1 else 0 end)) AS `document_count`,sum((case when (`r`.`type` = 'video') then 1 else 0 end)) AS `video_count`,sum((case when (`r`.`type` = 'code') then 1 else 0 end)) AS `code_count`,sum(`r`.`download_count`) AS `total_downloads`,sum(`r`.`file_size`) AS `total_size` from (`tb_course` `c` left join `tb_resource` `r` on(((`c`.`id` = `r`.`course_id`) and (`r`.`status` = 'approved')))) group by `c`.`id`,`c`.`name`,`c`.`teacher_name`;

-- ----------------------------
-- View structure for v_exam_paper_statistics
-- ----------------------------
DROP VIEW IF EXISTS `v_exam_paper_statistics`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_exam_paper_statistics` AS select `ep`.`id` AS `exam_paper_id`,`ep`.`course_id` AS `course_id`,`ep`.`course_name` AS `course_name`,`ep`.`title` AS `exam_title`,`ep`.`total_score` AS `total_score`,`ep`.`duration` AS `duration`,`ep`.`start_time` AS `start_time`,`ep`.`end_time` AS `end_time`,`ep`.`status` AS `status`,count(distinct `eq`.`question_id`) AS `question_count`,count(distinct `se`.`student_id`) AS `participant_count`,sum((case when (`se`.`status` = 'submitted') then 1 else 0 end)) AS `submitted_count`,sum((case when (`se`.`status` = 'graded') then 1 else 0 end)) AS `graded_count`,avg(`se`.`total_score`) AS `average_score`,max(`se`.`total_score`) AS `highest_score`,min(`se`.`total_score`) AS `lowest_score` from ((`tb_exam_paper` `ep` left join `tb_exam_question` `eq` on((`ep`.`id` = `eq`.`exam_paper_id`))) left join `tb_student_exam` `se` on((`ep`.`id` = `se`.`exam_paper_id`))) group by `ep`.`id`,`ep`.`course_id`,`ep`.`course_name`,`ep`.`title`,`ep`.`total_score`,`ep`.`duration`,`ep`.`start_time`,`ep`.`end_time`,`ep`.`status`;

-- ----------------------------
-- View structure for v_experiment_completion
-- ----------------------------
DROP VIEW IF EXISTS `v_experiment_completion`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_experiment_completion` AS select `e`.`id` AS `experiment_id`,`e`.`course_id` AS `course_id`,`e`.`course_name` AS `course_name`,`e`.`title` AS `experiment_title`,`e`.`deadline` AS `deadline`,count(`cs`.`student_id`) AS `total_students`,count(`er`.`id`) AS `submitted_count`,((count(`er`.`id`) * 100.0) / nullif(count(`cs`.`student_id`),0)) AS `completion_rate` from (((`tb_experiment` `e` join `tb_course` `c` on((`e`.`course_id` = `c`.`id`))) left join `tb_course_selection` `cs` on(((`c`.`id` = `cs`.`course_id`) and (`cs`.`status` = 'selected')))) left join `tb_experiment_report` `er` on((`e`.`id` = `er`.`experiment_id`))) group by `e`.`id`,`e`.`course_id`,`e`.`course_name`,`e`.`title`,`e`.`deadline`;

-- ----------------------------
-- View structure for v_file_preview_statistics
-- ----------------------------
DROP VIEW IF EXISTS `v_file_preview_statistics`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_file_preview_statistics` AS select `fp`.`resource_id` AS `resource_id`,`r`.`name` AS `resource_name`,`r`.`course_name` AS `course_name`,`fp`.`file_type` AS `file_type`,`fp`.`file_size` AS `file_size`,count(distinct `fp`.`viewer_id`) AS `unique_viewers`,sum(`fp`.`view_count`) AS `total_views`,max(`fp`.`last_view_time`) AS `last_view_time`,`fp`.`preview_type` AS `preview_type`,`fp`.`conversion_status` AS `conversion_status` from (`tb_file_preview` `fp` join `tb_resource` `r` on((`fp`.`resource_id` = `r`.`id`))) group by `fp`.`resource_id`,`r`.`name`,`r`.`course_name`,`fp`.`file_type`,`fp`.`file_size`,`fp`.`preview_type`,`fp`.`conversion_status`;

-- ----------------------------
-- View structure for v_homework_statistics
-- ----------------------------
DROP VIEW IF EXISTS `v_homework_statistics`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_homework_statistics` AS select `h`.`id` AS `homework_id`,`h`.`course_id` AS `course_id`,`h`.`course_name` AS `course_name`,`h`.`title` AS `homework_title`,`h`.`deadline` AS `deadline`,`h`.`score` AS `total_score`,count(`hs`.`id`) AS `submission_count`,sum((case when (`hs`.`status` = 'submitted') then 1 else 0 end)) AS `ungraded_count`,sum((case when (`hs`.`status` = 'graded') then 1 else 0 end)) AS `graded_count`,avg(`hs`.`grade`) AS `average_grade` from (`tb_homework` `h` left join `tb_homework_submission` `hs` on((`h`.`id` = `hs`.`homework_id`))) group by `h`.`id`,`h`.`course_id`,`h`.`course_name`,`h`.`title`,`h`.`deadline`,`h`.`score`;

-- ----------------------------
-- View structure for v_popular_resources
-- ----------------------------
DROP VIEW IF EXISTS `v_popular_resources`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_popular_resources` AS select `r`.`id` AS `id`,`r`.`name` AS `resource_name`,`r`.`type` AS `type`,`r`.`course_name` AS `course_name`,`r`.`uploader_name` AS `uploader_name`,`r`.`download_count` AS `download_count`,`r`.`file_size` AS `file_size`,`r`.`tags` AS `tags`,`r`.`create_time` AS `create_time`,rank() OVER (ORDER BY `r`.`download_count` desc )  AS `popularity_rank` from `tb_resource` `r` where (`r`.`status` = 'approved') order by `r`.`download_count` desc;

-- ----------------------------
-- View structure for v_question_usage_statistics
-- ----------------------------
DROP VIEW IF EXISTS `v_question_usage_statistics`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_question_usage_statistics` AS select `qb`.`id` AS `question_id`,`qb`.`question_type` AS `question_type`,`qb`.`difficulty` AS `difficulty`,left(`qb`.`content`,50) AS `content_preview`,`qb`.`score` AS `default_score`,`qb`.`tags` AS `tags`,`qb`.`creator_name` AS `creator_name`,count(distinct `eq`.`exam_paper_id`) AS `used_in_exams`,count(`sa`.`id`) AS `total_answers`,sum((case when (`sa`.`is_correct` = 1) then 1 else 0 end)) AS `correct_answers`,(case when (count(`sa`.`id`) > 0) then round(((sum((case when (`sa`.`is_correct` = 1) then 1 else 0 end)) * 100.0) / count(`sa`.`id`)),2) else NULL end) AS `correct_rate`,`qb`.`create_time` AS `create_time` from ((`tb_question_bank` `qb` left join `tb_exam_question` `eq` on((`qb`.`id` = `eq`.`question_id`))) left join `tb_student_answer` `sa` on((`qb`.`id` = `sa`.`question_id`))) where (`qb`.`status` = 'active') group by `qb`.`id`,`qb`.`question_type`,`qb`.`difficulty`,`qb`.`content`,`qb`.`score`,`qb`.`tags`,`qb`.`creator_name`,`qb`.`create_time`;

-- ----------------------------
-- View structure for v_recent_operations_summary
-- ----------------------------
DROP VIEW IF EXISTS `v_recent_operations_summary`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_recent_operations_summary` AS select 'student' AS `user_type`,count(0) AS `operation_count`,count(distinct `tb_student_operation_log`.`student_id`) AS `active_users`,count((case when (`tb_student_operation_log`.`status` = 'failed') then 1 end)) AS `failed_count` from `tb_student_operation_log` where (`tb_student_operation_log`.`operation_time` >= (now() - interval 24 hour)) union all select 'teacher' AS `user_type`,count(0) AS `operation_count`,count(distinct `tb_teacher_operation_log`.`teacher_id`) AS `active_users`,count((case when (`tb_teacher_operation_log`.`status` = 'failed') then 1 end)) AS `failed_count` from `tb_teacher_operation_log` where (`tb_teacher_operation_log`.`operation_time` >= (now() - interval 24 hour)) union all select 'admin' AS `user_type`,count(0) AS `operation_count`,count(distinct `tb_admin_operation_log`.`admin_id`) AS `active_users`,count((case when (`tb_admin_operation_log`.`status` = 'failed') then 1 end)) AS `failed_count` from `tb_admin_operation_log` where (`tb_admin_operation_log`.`operation_time` >= (now() - interval 24 hour));

-- ----------------------------
-- View structure for v_student_activity_stats
-- ----------------------------
DROP VIEW IF EXISTS `v_student_activity_stats`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_student_activity_stats` AS select `s`.`student_id` AS `student_id`,`s`.`student_name` AS `student_name`,`s`.`student_number` AS `student_number`,count(0) AS `total_operations`,count((case when (`s`.`module` = 'course') then 1 end)) AS `course_views`,count((case when (`s`.`module` = 'homework') then 1 end)) AS `homework_submits`,count((case when (`s`.`module` = 'exam') then 1 end)) AS `exam_attempts`,count((case when (`s`.`module` = 'resource') then 1 end)) AS `resource_downloads`,count((case when (`s`.`module` = 'discussion') then 1 end)) AS `discussion_posts`,count((case when (`s`.`status` = 'failed') then 1 end)) AS `failed_operations`,max(`s`.`operation_time`) AS `last_activity_time` from `tb_student_operation_log` `s` group by `s`.`student_id`,`s`.`student_name`,`s`.`student_number`;

-- ----------------------------
-- View structure for v_student_answer_detail
-- ----------------------------
DROP VIEW IF EXISTS `v_student_answer_detail`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_student_answer_detail` AS select `sa`.`id` AS `answer_id`,`sa`.`student_exam_id` AS `student_exam_id`,`se`.`exam_paper_id` AS `exam_paper_id`,`ep`.`title` AS `exam_title`,`se`.`student_name` AS `student_name`,`sa`.`question_id` AS `question_id`,`qb`.`question_type` AS `question_type`,`qb`.`difficulty` AS `difficulty`,`qb`.`content` AS `question_content`,`qb`.`options` AS `question_options`,`qb`.`answer` AS `correct_answer`,`sa`.`student_answer` AS `student_answer`,`sa`.`is_correct` AS `is_correct`,`sa`.`score` AS `earned_score`,`eq`.`question_score` AS `full_score`,`qb`.`explanation` AS `explanation`,`sa`.`answer_time` AS `answer_time` from ((((`tb_student_answer` `sa` join `tb_student_exam` `se` on((`sa`.`student_exam_id` = `se`.`id`))) join `tb_exam_paper` `ep` on((`se`.`exam_paper_id` = `ep`.`id`))) join `tb_question_bank` `qb` on((`sa`.`question_id` = `qb`.`id`))) join `tb_exam_question` `eq` on(((`ep`.`id` = `eq`.`exam_paper_id`) and (`qb`.`id` = `eq`.`question_id`))));

-- ----------------------------
-- View structure for v_student_course_detail
-- ----------------------------
DROP VIEW IF EXISTS `v_student_course_detail`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_student_course_detail` AS select `cs`.`id` AS `selection_id`,`cs`.`student_id` AS `student_id`,`cs`.`student_name` AS `student_name`,`s`.`student_number` AS `student_number`,`s`.`major` AS `major`,`s`.`class_name` AS `class_name`,`cs`.`course_id` AS `course_id`,`cs`.`course_name` AS `course_name`,`c`.`course_code` AS `course_code`,`c`.`teacher_name` AS `teacher_name`,`c`.`semester` AS `semester`,`c`.`credit` AS `credit`,`cs`.`select_time` AS `select_time`,`cs`.`status` AS `selection_status`,`cs`.`final_score` AS `final_score` from ((`tb_course_selection` `cs` join `tb_student` `s` on((`cs`.`student_id` = `s`.`id`))) join `tb_course` `c` on((`cs`.`course_id` = `c`.`id`)));

-- ----------------------------
-- View structure for v_student_exam_scores
-- ----------------------------
DROP VIEW IF EXISTS `v_student_exam_scores`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_student_exam_scores` AS select `se`.`id` AS `exam_record_id`,`se`.`exam_paper_id` AS `exam_paper_id`,`ep`.`title` AS `exam_title`,`ep`.`course_name` AS `course_name`,`se`.`student_id` AS `student_id`,`se`.`student_name` AS `student_name`,`se`.`student_number` AS `student_number`,`s`.`major` AS `major`,`s`.`class_name` AS `class_name`,`se`.`start_time` AS `start_time`,`se`.`submit_time` AS `submit_time`,timestampdiff(MINUTE,`se`.`start_time`,`se`.`submit_time`) AS `used_minutes`,`ep`.`duration` AS `total_minutes`,`se`.`total_score` AS `total_score`,`ep`.`total_score` AS `full_score`,((`se`.`total_score` * 100.0) / `ep`.`total_score`) AS `score_percent`,(case when (`se`.`total_score` >= `ep`.`pass_score`) then '及格' else '不及格' end) AS `pass_status`,(case when (`se`.`total_score` >= (`ep`.`total_score` * 0.9)) then '优秀' when (`se`.`total_score` >= (`ep`.`total_score` * 0.8)) then '良好' when (`se`.`total_score` >= (`ep`.`total_score` * 0.7)) then '中等' when (`se`.`total_score` >= `ep`.`pass_score`) then '及格' else '不及格' end) AS `grade_level`,`se`.`status` AS `status` from ((`tb_student_exam` `se` join `tb_exam_paper` `ep` on((`se`.`exam_paper_id` = `ep`.`id`))) join `tb_student` `s` on((`se`.`student_id` = `s`.`id`))) where (`se`.`status` in ('submitted','graded'));

-- ----------------------------
-- View structure for v_student_info
-- ----------------------------
DROP VIEW IF EXISTS `v_student_info`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_student_info` AS select `u`.`id` AS `user_id`,`u`.`username` AS `username`,`u`.`real_name` AS `real_name`,`u`.`gender` AS `gender`,`u`.`email` AS `email`,`u`.`phone` AS `phone`,`u`.`avatar` AS `avatar`,`u`.`status` AS `status`,`s`.`id` AS `student_id`,`s`.`student_number` AS `student_number`,`s`.`major` AS `major`,`s`.`class_name` AS `class_name`,`s`.`enrollment_year` AS `enrollment_year`,`s`.`grade` AS `grade` from (`tb_user` `u` join `tb_student` `s` on((`u`.`id` = `s`.`user_id`))) where (`u`.`role` = 'student');

-- ----------------------------
-- View structure for v_student_learning_progress
-- ----------------------------
DROP VIEW IF EXISTS `v_student_learning_progress`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_student_learning_progress` AS select `lp`.`student_id` AS `student_id`,`s`.`student_number` AS `student_number`,`u`.`real_name` AS `student_name`,`lp`.`course_id` AS `course_id`,`c`.`name` AS `course_name`,count(`lp`.`resource_id`) AS `total_resources`,sum((case when (`lp`.`is_completed` = 1) then 1 else 0 end)) AS `completed_resources`,round(((sum((case when (`lp`.`is_completed` = 1) then 1 else 0 end)) * 100.0) / count(`lp`.`resource_id`)),2) AS `completion_rate`,sum(`lp`.`duration_seconds`) AS `total_study_seconds`,round((sum(`lp`.`duration_seconds`) / 3600.0),2) AS `total_study_hours`,max(`lp`.`last_study_time`) AS `last_study_time` from (((`tb_learning_progress` `lp` join `tb_student` `s` on((`lp`.`student_id` = `s`.`id`))) join `tb_user` `u` on((`s`.`user_id` = `u`.`id`))) join `tb_course` `c` on((`lp`.`course_id` = `c`.`id`))) group by `lp`.`student_id`,`s`.`student_number`,`u`.`real_name`,`lp`.`course_id`,`c`.`name`;

-- ----------------------------
-- View structure for v_student_transcript
-- ----------------------------
DROP VIEW IF EXISTS `v_student_transcript`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_student_transcript` AS select `s`.`id` AS `student_id`,`s`.`student_number` AS `student_number`,`u`.`real_name` AS `student_name`,`s`.`major` AS `major`,`s`.`class_name` AS `class_name`,`cs`.`course_id` AS `course_id`,`cs`.`course_name` AS `course_name`,`c`.`course_code` AS `course_code`,`c`.`semester` AS `semester`,`c`.`credit` AS `credit`,`cs`.`final_score` AS `final_score`,(case when (`cs`.`final_score` >= 90) then '优秀' when (`cs`.`final_score` >= 80) then '良好' when (`cs`.`final_score` >= 70) then '中等' when (`cs`.`final_score` >= 60) then '及格' else '不及格' end) AS `grade_level` from (((`tb_student` `s` join `tb_user` `u` on((`s`.`user_id` = `u`.`id`))) join `tb_course_selection` `cs` on((`s`.`id` = `cs`.`student_id`))) join `tb_course` `c` on((`cs`.`course_id` = `c`.`id`))) where (`cs`.`status` in ('selected','completed'));

-- ----------------------------
-- View structure for v_system_statistics
-- ----------------------------
DROP VIEW IF EXISTS `v_system_statistics`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_system_statistics` AS select (select count(0) from `tb_user` where ((`tb_user`.`role` = 'student') and (`tb_user`.`status` = 'active'))) AS `active_student_count`,(select count(0) from `tb_user` where ((`tb_user`.`role` = 'teacher') and (`tb_user`.`status` = 'active'))) AS `active_teacher_count`,(select count(0) from `tb_user` where ((`tb_user`.`role` = 'admin') and (`tb_user`.`status` = 'active'))) AS `active_admin_count`,(select count(0) from `tb_course` where (`tb_course`.`status` = 'approved')) AS `approved_course_count`,(select count(0) from `tb_resource` where (`tb_resource`.`status` = 'approved')) AS `approved_resource_count`,(select sum(`tb_resource`.`file_size`) from `tb_resource` where (`tb_resource`.`status` = 'approved')) AS `total_storage_used`,(select count(0) from `tb_homework`) AS `total_homework_count`,(select count(0) from `tb_experiment`) AS `total_experiment_count`,(select count(0) from `tb_announcement` where (`tb_announcement`.`status` = 'published')) AS `published_announcement_count`;

-- ----------------------------
-- View structure for v_teacher_activity_stats
-- ----------------------------
DROP VIEW IF EXISTS `v_teacher_activity_stats`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_teacher_activity_stats` AS select `t`.`teacher_id` AS `teacher_id`,`t`.`teacher_name` AS `teacher_name`,`t`.`teacher_number` AS `teacher_number`,count(0) AS `total_operations`,count((case when (`t`.`module` = 'course') then 1 end)) AS `course_operations`,count((case when (`t`.`module` = 'homework') then 1 end)) AS `homework_operations`,count((case when (`t`.`module` = 'exam') then 1 end)) AS `exam_operations`,count((case when (`t`.`module` = 'resource') then 1 end)) AS `resource_operations`,count((case when (`t`.`module` = 'grading') then 1 end)) AS `grading_operations`,count((case when (`t`.`status` = 'failed') then 1 end)) AS `failed_operations`,max(`t`.`operation_time`) AS `last_activity_time` from `tb_teacher_operation_log` `t` group by `t`.`teacher_id`,`t`.`teacher_name`,`t`.`teacher_number`;

-- ----------------------------
-- View structure for v_teacher_course_statistics
-- ----------------------------
DROP VIEW IF EXISTS `v_teacher_course_statistics`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_teacher_course_statistics` AS select `t`.`id` AS `teacher_id`,`t`.`teacher_number` AS `teacher_number`,`u`.`real_name` AS `teacher_name`,`t`.`department` AS `department`,count(`c`.`id`) AS `course_count`,sum(`c`.`enrolled`) AS `total_students`,avg(((`c`.`enrolled` * 1.0) / `c`.`capacity`)) AS `avg_enrollment_rate` from ((`tb_teacher` `t` join `tb_user` `u` on((`t`.`user_id` = `u`.`id`))) left join `tb_course` `c` on(((`t`.`id` = `c`.`teacher_id`) and (`c`.`status` = 'approved')))) group by `t`.`id`,`t`.`teacher_number`,`u`.`real_name`,`t`.`department`;

-- ----------------------------
-- View structure for v_teacher_info
-- ----------------------------
DROP VIEW IF EXISTS `v_teacher_info`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_teacher_info` AS select `u`.`id` AS `user_id`,`u`.`username` AS `username`,`u`.`real_name` AS `real_name`,`u`.`gender` AS `gender`,`u`.`email` AS `email`,`u`.`phone` AS `phone`,`u`.`avatar` AS `avatar`,`u`.`status` AS `status`,`t`.`id` AS `teacher_id`,`t`.`teacher_number` AS `teacher_number`,`t`.`department` AS `department`,`t`.`title` AS `title`,`t`.`research_field` AS `research_field` from (`tb_user` `u` join `tb_teacher` `t` on((`u`.`id` = `t`.`user_id`))) where (`u`.`role` = 'teacher');

-- ----------------------------
-- View structure for v_teacher_workload
-- ----------------------------
DROP VIEW IF EXISTS `v_teacher_workload`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_teacher_workload` AS select `t`.`id` AS `teacher_id`,`t`.`teacher_number` AS `teacher_number`,`u`.`real_name` AS `teacher_name`,`t`.`department` AS `department`,count(distinct `c`.`id`) AS `course_count`,sum(`c`.`enrolled`) AS `total_students`,count(distinct `h`.`id`) AS `homework_count`,count(distinct `e`.`id`) AS `experiment_count`,count(distinct `ep`.`id`) AS `exam_count`,count(distinct `r`.`id`) AS `resource_count`,count(distinct `hs`.`id`) AS `homework_to_grade`,count(distinct `se`.`id`) AS `exam_to_grade` from ((((((((`tb_teacher` `t` join `tb_user` `u` on((`t`.`user_id` = `u`.`id`))) left join `tb_course` `c` on(((`t`.`id` = `c`.`teacher_id`) and (`c`.`status` = 'approved')))) left join `tb_homework` `h` on((`c`.`id` = `h`.`course_id`))) left join `tb_experiment` `e` on((`c`.`id` = `e`.`course_id`))) left join `tb_exam_paper` `ep` on((`c`.`id` = `ep`.`course_id`))) left join `tb_resource` `r` on(((`c`.`id` = `r`.`course_id`) and (`r`.`status` = 'approved')))) left join `tb_homework_submission` `hs` on(((`h`.`id` = `hs`.`homework_id`) and (`hs`.`status` = 'submitted')))) left join `tb_student_exam` `se` on(((`ep`.`id` = `se`.`exam_paper_id`) and (`se`.`status` = 'submitted')))) group by `t`.`id`,`t`.`teacher_number`,`u`.`real_name`,`t`.`department`;

-- ----------------------------
-- View structure for v_weekly_schedule
-- ----------------------------
DROP VIEW IF EXISTS `v_weekly_schedule`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_weekly_schedule` AS select `s`.`id` AS `id`,`s`.`course_id` AS `course_id`,`s`.`course_name` AS `course_name`,`s`.`teacher_name` AS `teacher_name`,`s`.`classroom` AS `classroom`,`s`.`day_of_week` AS `day_of_week`,(case `s`.`day_of_week` when 1 then '周一' when 2 then '周二' when 3 then '周三' when 4 then '周四' when 5 then '周五' when 6 then '周六' when 7 then '周日' end) AS `day_name`,`s`.`period` AS `period`,(case `s`.`period` when 1 then '08:00-09:40' when 2 then '10:00-11:40' when 3 then '14:00-15:40' when 4 then '16:00-17:40' when 5 then '19:00-20:40' end) AS `time_slot`,`s`.`start_week` AS `start_week`,`s`.`end_week` AS `end_week`,`s`.`semester` AS `semester` from `tb_schedule` `s`;

-- ----------------------------
-- Procedure structure for sp_batch_import_students
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_batch_import_students`;
delimiter ;;

;;
delimiter ;

-- ----------------------------
-- Procedure structure for sp_calculate_student_gpa
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_calculate_student_gpa`;
delimiter ;;

;;
delimiter ;

-- ----------------------------
-- Procedure structure for sp_check_schedule_conflict
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_check_schedule_conflict`;
delimiter ;;

;;
delimiter ;

-- ----------------------------
-- Procedure structure for sp_cleanup_expired_data
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_cleanup_expired_data`;
delimiter ;;

;;
delimiter ;

-- ----------------------------
-- Procedure structure for sp_course_grade_distribution
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_course_grade_distribution`;
delimiter ;;

;;
delimiter ;

-- ----------------------------
-- Procedure structure for sp_drop_course
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_drop_course`;
delimiter ;;

;;
delimiter ;

-- ----------------------------
-- Procedure structure for sp_generate_random_exam
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_generate_random_exam`;
delimiter ;;

;;
delimiter ;

-- ----------------------------
-- Procedure structure for sp_grade_subjective_questions
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_grade_subjective_questions`;
delimiter ;;

;;
delimiter ;

-- ----------------------------
-- Procedure structure for sp_select_course
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_select_course`;
delimiter ;;

;;
delimiter ;

-- ----------------------------
-- Procedure structure for sp_start_exam
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_start_exam`;
delimiter ;;

;;
delimiter ;

-- ----------------------------
-- Procedure structure for sp_submit_exam
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_submit_exam`;
delimiter ;;

;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
