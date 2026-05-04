import { Routes, Route, Navigate } from "react-router-dom";
import AppLayout from "../components/layout/AppLayout";
import DashboardPage from "../pages/DashboardPage";
import CompanyMeterCrudPage from "../pages/CompanyMeterCrudPage";
import CompanyContactPage from "../pages/CompanyContactPage.jsx";
// import CompanyPage from "../pages/CompanyPage";
// import MeterPage from "../pages/MeterPage";
// import ReportPage from "../pages/ReportPage";
import SettingsPage from "../pages/SettingsPage";

function AppRoutes() {
    return (
        <Routes>
            <Route path="/" element={<AppLayout />}>
                <Route index element={<Navigate to="/dashboard" replace />} />
                <Route path="dashboard" element={<DashboardPage />} />
                <Route path="company-meter" element={<CompanyMeterCrudPage />} />
                <Route path="company-contacts" element={<CompanyContactPage />} />
                {/* <Route path="company" element={<CompanyPage />} />
                <Route path="meter" element={<MeterPage />} />
                <Route path="report" element={<ReportPage />} /> */}
                <Route path="settings" element={<SettingsPage />} />
            </Route>
        </Routes>
    );
}

export default AppRoutes;
