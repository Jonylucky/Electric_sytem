package com.index_electric_server.device_system_electric_server.util;

import com.index_electric_server.device_system_electric_server.dto.DrawingPanelCoordDto;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility for converting raw PDF coordinate data
 * (output from pdfplumber Python script) into DrawingPanelCoord requests.
 *
 * Input JSON from Python looks like:
 * [{"panel_code": "TDT-T1", "x_pts": 350.7, "y_pts": 418.3,
 *   "page_width": 1001.0, "page_height": 709.0}]
 */
@UtilityClass
public class CoordExtractUtil {

    /**
     * Convert raw PDF coordinates (points) to percent-based coordinates.
     *
     * @param xPts       raw x in PDF points
     * @param yPts       raw y in PDF points
     * @param pageWidth  PDF page width in points
     * @param pageHeight PDF page height in points
     * @return [xPercent, yPercent] rounded to 2 decimal places
     */
    public BigDecimal[] toPercent(double xPts, double yPts,
                                   double pageWidth, double pageHeight) {
        BigDecimal xPct = BigDecimal.valueOf(xPts / pageWidth * 100)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal yPct = BigDecimal.valueOf(yPts / pageHeight * 100)
            .setScale(2, RoundingMode.HALF_UP);
        return new BigDecimal[]{xPct, yPct};
    }

    /**
     * Build a batch of coord requests from Python extraction output.
     * Each map must contain: drawing_id, panel_id, x_pts, y_pts, page_width, page_height
     */
    public List<DrawingPanelCoordDto.Request> fromRawList(List<Map<String, Object>> rawList) {
        return rawList.stream().map(raw -> {
            double xPts      = toDouble(raw.get("x_pts"));
            double yPts      = toDouble(raw.get("y_pts"));
            double pageWidth  = toDouble(raw.get("page_width"));
            double pageHeight = toDouble(raw.get("page_height"));

            BigDecimal[] pct = toPercent(xPts, yPts, pageWidth, pageHeight);

            return DrawingPanelCoordDto.Request.builder()
                .drawingId(toLong(raw.get("drawing_id")))
                .panelId(toLong(raw.get("panel_id")))
                .xPercent(pct[0])
                .yPercent(pct[1])
                .labelAnchor((String) raw.getOrDefault("label_anchor", "top"))
                .extractMethod("pdfplumber")
                .confidence(raw.get("confidence") != null
                    ? BigDecimal.valueOf(toDouble(raw.get("confidence"))) : null)
                .build();
        }).collect(Collectors.toList());
    }

    private double toDouble(Object val) {
        if (val instanceof Number n) return n.doubleValue();
        return Double.parseDouble(val.toString());
    }

    private Long toLong(Object val) {
        if (val instanceof Number n) return n.longValue();
        return Long.parseLong(val.toString());
    }
}
