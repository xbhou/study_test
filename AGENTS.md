# AGENTS.md

## 文档语言规范 / Documentation Language Convention

本仓库中的学习文档、路线图、源码阅读笔记和新增说明，默认使用 **中文 + English 双语**。  
Learning documents, roadmaps, source-reading notes, and newly added explanatory documentation in this repository should be **bilingual: Chinese + English** by default.

### 写作规则 / Writing Rules

1. 标题采用“中文 / English”格式。  
   Use the format “中文 / English” for headings.

2. 解释性内容优先中文，随后给出对应英文。  
   Put Chinese first for explanatory prose, followed by the corresponding English.

3. Checklist 项目使用“中文 / English”放在同一行，尽量保持简洁。  
   Keep checklist items bilingual on the same line when practical.

4. 代码、类名、方法名、异常名、协议名和标准技术术语保留英文，不强行翻译。  
   Keep code, class names, method names, exception names, protocol names, and standard technical terms in English.

5. Mermaid、ASCII 架构图和代码块中的技术标识优先使用英文；必要时在图外补充中文解释。  
   Prefer English technical labels in Mermaid, ASCII architecture diagrams, and code blocks; add Chinese explanation outside the diagram when needed.

6. 新增 `docs/` 下的学习文档时，默认遵循此规范，除非用户明确要求只使用一种语言。  
   New learning documents under `docs/` should follow this convention unless the user explicitly requests a single language.

7. Commit message 保持英文、简洁、符合常见 Git 习惯。  
   Keep commit messages concise and in English, following common Git conventions.

## 学习内容风格 / Learning Content Style

- 以问题驱动，而不是按源码文件顺序机械阅读。  
  Be question-driven instead of reading source files mechanically in order.
- 优先形成“问题 → 源码 → 实验 → 结论 → 架构启发”的闭环。  
  Prefer the loop “problem → source code → experiment → conclusion → architecture insight”.
- 示例尽量与 Java 后端、分布式系统、RPC、缓存、Kafka、数据库和架构设计相关。  
  Prefer examples related to Java backend, distributed systems, RPC, caching, Kafka, databases, and architecture design.
