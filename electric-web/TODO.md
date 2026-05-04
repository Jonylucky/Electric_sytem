# CompanyContact CRUD Implementation Plan

## Status: ✅ Plan Approved - Implementing Step-by-Step
a
### Priority Steps:

#### 1. ✅ Create CompanyContactPage.jsx (src/pages/CompanyContactPage.jsx)
- [✅] Complete CRUD page with Table + Dialog form in single file  
- [✅] Test UI + basic logic (placeholder APIs working)

#### 2. ✅ Add companyContact API functions
- [✅] Added 4 CRUD functions to `src/api/companyApi.js`
- [✅] Connected to CompanyContactPage.jsx
- [✅] Correct payload format & API endpoints

#### 3. ✅ Add route
- [✅] Added `/company-contacts` route to `src/routes/AppRoutes.jsx`
- [✅] Page accessible at `/company-contacts`

#### 4. ✅ Add Sidebar navigation
- [✅] Added "Liên hệ công ty" menu item to Sidebar.jsx with ContactPhoneIcon

#### 5. ✅ COMPLETE - Ready for Testing!
- [✅] Run `npm run dev` 
- [✅] Navigate `/company-contacts` or Sidebar → "Liên hệ công ty"
- [✅] All CRUD ops connected with correct API endpoints & payload
- [✅] UI/UX matches existing pages (CompanyMeterCrudPage pattern)

### Notes:
- Follow CompanyMeterCrudPage.jsx pattern (Table left? No, single page with table + dialog)
- MUI components, no TS
- Snackbar for UX, loading states
- contactType options: admin, accountant, technical, manager
