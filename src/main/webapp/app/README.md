#app/ directory structure

├──  static/
│   ├──  app/
│   │   ├──  components/
│   │   │   ├──  <component-1>/
│   │   │   ├──  <component-2>/
│   │   │   ├──  <component-3>/
│   │   │   ├──  <component-..>/
│   │   ├──  admin/
│   │   ├──  login/
│   │   ├──  <module-1>/
│   │   ├──  <module-2>/
│   │   ├──  <module-3>/
│   │   ├──  <module-..>/
│   │   └──  processapp.js

- app/ ~ Main folder
- app/<module-x> ~ A folder represents a complete feature/functionality.
- app/components/ ~ Parent folder for components
- app/components/<component-x> ~ A folder represents a complete feature/functionality that is shared among modules (e.g. footer, sidebar, etc.).
