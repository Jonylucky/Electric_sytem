import axios from "axios";
import apiClient from "./apiClient";


export const sendMailByCompanyId = async ({
  companyId,
  month,
  subject,
  content,
  html = true,
}) => {
  return apiClient.post(`/mail/send/company/${companyId}/zip`, {
    month,
    subject,
    content,
    html,
  });
};