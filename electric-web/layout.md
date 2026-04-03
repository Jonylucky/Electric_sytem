# Project Layout Analysis - Electric Web

## 1. Framework & Tech Stack
- **Framework**: React 19 (ES6 modules) + Vite (build tool, from `vite.config.js`, `main.jsx`).
- **Routing**: `react-router-dom` v7 (AppRoutes.jsx, Outlet in AppLayout).
- **State Management**: None explicit (useState/useEffect only in samples).
- **Charts**: Recharts.
- **HTTP**: Axios.
- **Other**: Dayjs (dates), File-saver (downloads), SockJS/StompJS (WebSockets), @mui/x-data-grid (tables).
- **Build/Deploy**: Vite, Docker (Dockerfile/docker-compose.yml/nginx.conf).

## 2. UI Library
- **Primary**: **MUI (Material-UI) v7** (`@mui/material`, `@mui/icons-material`, `@emotion/react/styled`).
  - Heavy usage: Box, Stack, Paper, Card, Typography, Button, TextField, Table/Table*, Dialog, Drawer, AppBar, Chip, IconButton, MenuItem, Select, FormControl, Alert, CircularProgress.
  - Styled components with `styled('main')`, `theme.spacing()`, `theme.transitions`.
  - Data tables: Custom MUI Table (CompanyMeterList), likely DataGrid elsewhere.
- **No**: Tailwind, shadcn, Antd, Chakra, Bootstrap.

## 3. Directory Structure
```
src/
├── api/          # API clients (companyApi.js, meterApi.js, etc.) - Axios wrappers
├── assets/       # Static images (react.svg)
├── components/
│   ├── charts/   # Recharts (ConsumptionChart.jsx)
│   └── layout/   # Core layout/UI (AppLayout.jsx, HeaderBar.jsx, Sidebar.jsx, Footer.jsx, forms/lists)
├── pages/        # Page components (DashboardPage.jsx, CompanyPage.jsx, etc.) - Route views
├── routes/       # AppRoutes.jsx (Routes/Outlet)
├── services/     # WebSocket (websocketService.js)
└── utils/        # Helpers (downloadFile.js)
```
- **Flat & Logical**: Pages dir for routes, layout subdir for shared UI, api/services/utils separated.
- **No hooks/context dir** (local state only).

## 4. Page Structure
- **Pages-as-Routes**: Each page (e.g., DashboardPage.jsx) is a full view with Grid/Card/Table/Charts.
- **Layout Wrapper**: AppLayout (Header + Sidebar + Outlet + Footer).
  - Responsive: Permanent Sidebar desktop, temporary Drawer mobile.
- **Nested**: No deep nesting; flat routes.

## 5. Component Naming
- **PascalCase Descriptive**: `CompanyMeterForm`, `CompanyMeterList`, `ConsumptionChart`, `StatCard`, `StatusChip`.
- **Domain-Specific**: Prefixes like `CompanyMeter*` group related (CRUD form/list).
- **Layout Core**: HeaderBar, Sidebar, Footer, AppLayout.

## 6. Forms
- **Library**: Pure MUI (TextField, Select, Button, Chip for modes).
- **Pattern**:
  - Controlled `useState` forms.
  - Modes (create/update/full/meter-only/company-only).
  - Validation inline (required, helperText).
  - Sections: Stacked Paper/Card with alpha bg, Typography sections.
  - Submit: Async API calls (createCompany/updateMeter).
  - UX: Chips for modes, loading states, success/error callbacks.

## 7. Tables
- **Primary**: Custom MUI `Table`/`TableContainer` (stickyHeader, hover rows).
  - Sections: Dual tables (Company list + Meter list) in CompanyMeterList.
  - Filters: MUI Select.
  - Actions: Edit IconButton.
  - Highlight: Active edit row (bg alpha).
- **Advanced**: `@mui/x-data-grid` available (not seen in samples).

## 8. UI Components Patterns
| Component | MUI Base | Patterns |
|-----------|----------|----------|
| **Modal/Dialog** | Dialog | Fullscreen images w/zoom (Dashboard), chart modals. |
| **Drawer** | MuiDrawer | Responsive (permanent desktop, temp mobile), width=280px. |
| **Card/Paper** | Card/Paper | Elevation 2-3, borderRadius=3, section headers w/alpha bg (primary/secondary colors). |
| **Button** | Button (contained/outlined) | Custom bg/hover (1976d2 blue, ed6c02 orange), fullWidth, icons (DownloadIcon), loading. |
| **Header** | AppBar/Toolbar | Logo/title left, notifications/user menu right. |
| **Sidebar** | List/ListItemButton | NavLink active states, icons, fixed width 280px. |
| **Charts** | Recharts (ResponsiveContainer) | Bar/Line, custom colors, integrated in Cards. |

## 9. Styling (Colors, Spacing, Fonts)
- **Fonts**: `system-ui, Avenir, Helvetica, Arial` (index.css :root).
- **Colors**:
  - Primary: `#1976d2` (blue - buttons, sections).
  - Secondary: `#9c27b0` (purple - meter sections).
  - Warning: `#ed6c02` (orange - edit/update).
  - Alpha overlays: `alpha('#1976d2', 0.03/0.05)` for sections.
  - Dark/Light scheme support (index.css).
- **Spacing**: MUI `theme.spacing(2/2.5/3)`, `sx={{ p: 2/3 }}`, Stack/Gap.
- **Custom CSS**: index.css/App.css (minimal: buttons, cards, animations, no Tailwind).
- **Theme**: Implicit MUI default (no ThemeProvider in samples; likely in AppRoutes/main).

## 🎯 Project Style Summary
- **Style**: **MUI Dashboard Admin** - Clean, responsive, data-heavy (tables/charts/forms). Professional blue/orange/purple theme. Vite+React flat structure.
- **Reuse Heavily**:
  - Layout: AppLayout + HeaderBar/Sidebar/Footer.
  - Patterns: Paper/Card sections, Stack spacing, alpha bg headers, IconButtons actions.
  - Components: StatCard, StatusChip, custom Tables.
  - Utils: API wrappers, normalize data funcs.
- **Avoid/Dislikes (Don't Reinvent)**:
  - ❌ No new CSS/Tailwind - Stick to MUI sx/styled.
  - ❌ Custom tables/charts - Use MUI Table/DataGrid + Recharts.
  - ❌ Global state (Redux) - Local useState suffices.
  - ❌ Inline styles - Use `sx` or `styled`.
  - ❌ New UI libs - All MUI.
  - ❌ Deep nesting - Flat pages + Outlet.

**Recommendation**: New features? Copy CompanyMeterForm/List patterns (Paper forms, dual Tables). Add to `components/layout/`. Export via ZIP like Dashboard.

*Generated from code analysis (package.json, AppLayout, Form/List, Dashboard, Header/Sidebar).*  

