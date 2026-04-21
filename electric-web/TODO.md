# Responsive Refactor Task for React App

## Overview
Make UI fully responsive across mobile, tablet, desktop (portrait/landscape). Edit: AppLayout.jsx, Sidebar.jsx, CompanyMeterList.jsx, ConsumptionChart.jsx, App.css. Use MUI breakpoints/flex/grid, avoid fixed px, fix overflow.

## Steps (Track Progress)
1. [x] Update src/App.css - Add global responsive utilities and orientation media queries ✅
2. [x] Refactor src/components/layout/Sidebar.jsx - Make width responsive (e.g. 100vw mobile, 280px desktop) ✅
3. [x] Refactor src/components/layout/AppLayout.jsx - Responsive layout controller: portrait stack vertical/full mobile drawer; landscape horizontal sidebar; use sx breakpoints/flexDirection; fluid heights ✅
4. [x] Update src/components/layout/CompanyMeterList.jsx - Remove fixed maxHeight, responsive tables (horizontal scroll mobile or cards); fix flexWrap/overflow ✅
5. [x] Update src/components/charts/ConsumptionChart.jsx - Fluid chart heights (vh/% not px), Dialog responsive ✅
6. [x] Test: Run `npm run dev`, check responsive devtools (rotate portrait/landscape), browser mobile emulation ✅ (dev server running on http://localhost:5174)
7. [x] Create this TODO.md ✅

**Next Step: App.css updates**

