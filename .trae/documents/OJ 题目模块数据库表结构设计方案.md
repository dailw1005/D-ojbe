OJ 题目模块数据库表结构设计方案

### 1. 核心设计思路
*   **JSON 存储复杂配置**：对于判题配置（时间/内存限制）、测试用例、判题结果等结构化数据，使用 JSON 字符串存储，兼顾灵活性和开发效率。
*   **读写分离与冗余**：在题目表中冗余 `tags` 字段（JSON 数组）以便列表页快速展示，同时保留 `tag` 关联表用于复杂的标签检索。
*   **扩展性**：预留了题解、代码模板等表的结构，支持后续功能扩展。

### 2. 拟定创建的 SQL 文件
我将在 `sql` 目录下创建一个新的 SQL 文件 `question_table.sql`，包含以下表结构：

#### 核心业务表
1.  **`question` (题目表)**
    *   核心字段：`title`, `content`, `tags` (列表展示用), `answer` (参考答案), `difficulty`.
    *   判题字段：`judge_case` (测试用例), `judge_config` (时间/内存限制).
    *   统计字段：`submit_num`, `accepted_num`, `thumb_num`, `favour_num`.
2.  **`question_submit` (题目提交表)**
    *   记录用户的每一次代码提交。
    *   字段：`language`, `code`, `judge_info` (判题结果 JSON), `status` (判题状态).

#### 扩展功能表
3.  **`question_template` (题目代码模板表)**
    *   支持不同语言的初始代码骨架（如 Java 的 `class Solution...`）。
4.  **`question_solution` (题解表)**
    *   支持用户或官方发布题解，包含点赞、评论数等。
5.  **`tag` & `question_tag` (标签体系)**
    *   用于更规范的题目分类和检索。

### 3. 下一步行动
确认方案后，我将：
1.  在 `sql/` 目录下创建 `question_table.sql` 文件并写入完整的 DDL 语句。
2.  确保字段命名和类型与你现有的 `user_table.sql` 风格保持一致（如使用 `deleted` 做逻辑删除，`create_time` 自动填充等）。

这套结构既能满足 MVP（最小可行性产品）的快速开发，也支撑未来的社区化（题解、评论）和多语言扩展。