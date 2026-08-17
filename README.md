# DPHarmon.github.io

Source for my Computer Science ePortfolio, built for CS-499 Computer Science
Capstone at Southern New Hampshire University.

<div align="center">
  <a href="https://dpharmon.github.io/" title="ePortfolio">
    <img src="https://img.shields.io/badge/View-Live_ePortfolio-blue.svg?style=for-the-badge&logo=githubpages" alt="View the live ePortfolio" />
  </a>
</div>

## About

The portfolio presents three artifacts from my degree, each rebuilt to
demonstrate a different area of computer science. Every artifact is here in both
its original and enhanced form, so the improvement can be read from the code
rather than taken on faith.

| Enhancement | Category | Artifact | Course |
|:---|:---|:---|:---|
| [One](https://dpharmon.github.io/Enhancement-One) | Software Design and Engineering | Warehouse Inventory App | CS-360 Mobile Architecture and Programming |
| [Two](https://dpharmon.github.io/Enhancement-Two) | Algorithms and Data Structures | Course Planner | CS-300 DSA: Analysis and Design |
| [Three](https://dpharmon.github.io/Enhancement-Three) | Databases | Travlr Getaways | CS-465 Full Stack Development I |

## Enhancements at a glance

**One — Warehouse Inventory App** (Java, Android, Room/SQLite)
Replaced unsalted SHA-256 password storage with salted, iterated PBKDF2-HMAC-SHA256
in a self-describing record, verified with constant-time comparison. Legacy
accounts authenticate and upgrade transparently on next login through lazy
migration. Tightened the manifest to least privilege, moved the runtime SMS
request to `ActivityResultLauncher`, and added an input validation layer with
JUnit coverage.

**Two — Course Planner** (C++)
Converted a binary search tree to a self-balancing AVL tree for guaranteed
O(log n) insert and search. Replaced two-pass QuickSort and binary search
prerequisite validation with a single file pass backed by a hash set. Restructured
one file into five layers: domain model, data structure, data access, application,
and entry point.

**Three — Travlr Getaways** (MEAN stack)
Designed a bookings collection referencing trips and users by `ObjectId`, with
three indexes each mapping to an identified query. Built a seed generator
producing eighteen months of weighted history, four MongoDB aggregation pipelines
exposed as protected endpoints, and an Angular dashboard rendering them with
Chart.js.

## Repository structure

```
.
├── index.md                  home page and professional self-assessment
├── Enhancement-One.md        Software Design and Engineering write-up
├── Enhancement-Two.md        Algorithms and Data Structures write-up
├── Enhancement-Three.md      Databases write-up
├── _config.yml               Jekyll configuration
├── assets/img/               figures and screenshots
├── docs/                     reflections and the Technical Design Document
└── enhancement/
    ├── CS360-InventoryApp/   original/ and enhanced/
    ├── CS300-CoursePlanner/  original/ and enhanced/
    └── CS465-travlr/         original/ and enhanced/
```

## Running the artifacts

**Course Planner** — open `ProjectTwo.cpp.sln` in Visual Studio and build. The
program reads `ABCU_Advising_Program_Input.csv` from the project directory.

**Warehouse Inventory App** — open the project folder in Android Studio, let
Gradle sync, and run on an emulator or device.

**Travlr Getaways** — `node_modules` is not committed; restore it with
`npm install` in the project root and again in `app_admin`. Create a `.env` in
the root with `JWT_SECRET` set to a random string, seed the database, then
`npm start`.

## Built with

GitHub Pages and Jekyll, using the Cayman theme.

---

Dylan Harmon · [GitHub](https://github.com/DPHarmon) · [ePortfolio](https://dpharmon.github.io/)
