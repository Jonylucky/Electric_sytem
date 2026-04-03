# Fix Meter Form Task

## Steps:
- [x] 1. Create TODO.md with plan breakdown
- [x] 2. Apply minimal edits to CompanyMeterForm.jsx:
  - ✓ Confirmed meterId TextField: `value={form.meterId}`, `onChange=handleFieldChange("meterId")`, `disabled={isUpdate}`
  - ✓ Confirmed companyId Select (for createMeterOnly): `value={form.companyId}`, `onChange=handleFieldChange("companyId")`, preloads `editingData.companyId` on edit
  - ✓ Confirmed meterName TextField: `value={form.meterName}`, `onChange=handleFieldChange("meterName")`, `required`
  - ✓ Updated locationId label to "Floor" + added helperText
  - ✓ Verified payload: sends `{meterId, companyId, meterName, locationId}` correctly
- [x] 3. Verified form bindings:
  - meterId: binds `form.meterId`, displays on edit (disabled), sent in payload
  - company: separate Select `form.companyId`, preloads editingData.companyId on edit, sent as companyId
  - meterName: binds `form.meterName`, required
  - floor: locationId select label "Floor", displays Floor in options, sent as locationId
  - Payload confirmed correct for backend
- [x] 4. Task complete: Minimal changes applied successfully

Current progress: Starting edits.

