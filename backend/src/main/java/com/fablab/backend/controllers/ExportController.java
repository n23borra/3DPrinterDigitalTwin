package com.fablab.backend.controllers;

import com.fablab.backend.models.RiskResult;
import com.fablab.backend.repositories.RiskResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final RiskResultRepository resultRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Exports risk calculation results in the requested format.
     *
     * @param format desired export format: {@code pdf}, {@code csv} or {@code json}
     * @return {@link ResponseEntity} providing the exported document as a {@link ByteArrayResource}, or {@code 400 Bad Request}
     *         when the format is unsupported
     * @throws IOException        if serialization of the results fails
     * @throws DocumentException  if PDF generation cannot be completed
     */
    @GetMapping("/{format}")
    public ResponseEntity<ByteArrayResource> export(@PathVariable String format) throws IOException, DocumentException {
        List<RiskResult> results = resultRepo.findAll();
        return switch (format.toLowerCase()) {
            case "pdf" -> exportPdf(results);
            case "csv" -> exportCsv(results);
            case "json" -> exportJson(results);
            default -> ResponseEntity.badRequest().body(null);
        };
    }

    private ResponseEntity<ByteArrayResource> exportJson(List<RiskResult> results) throws IOException {
        byte[] data = mapper.writeValueAsBytes(results);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=risk-results.json").contentType(MediaType.APPLICATION_JSON).body(new ByteArrayResource(data));
    }

    private ResponseEntity<ByteArrayResource> exportCsv(List<RiskResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("id,asset,threat,vulnerability,fr,status\n");
        for (RiskResult r : results) {
            sb.append(r.getId()).append(',').append(csv(r.getRisk().getAsset().getName())).append(',').append(csv(r.getRisk().getThreat().getLabel())).append(',').append(csv(r.getRisk().getVulnerability().getLabel())).append(',').append(r.getFr()).append(',').append(r.getStatus()).append('\n');
        }
        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=risk-results.csv").contentType(MediaType.valueOf("text/csv")).body(new ByteArrayResource(data));
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        String v = value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v + "\"";
        }
        return v;
    }

    private ResponseEntity<ByteArrayResource> exportPdf(List<RiskResult> results) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();
        document.add(new Paragraph("Risk Results"));
        PdfPTable table = new PdfPTable(6);
        Stream.of("ID", "Asset", "Threat", "Vulnerability", "FR", "Status").forEach(h -> table.addCell(new PdfPCell(new Phrase(h))));
        for (RiskResult r : results) {
            table.addCell(String.valueOf(r.getId()));
            table.addCell(r.getRisk().getAsset().getName());
            table.addCell(r.getRisk().getThreat().getLabel());
            table.addCell(r.getRisk().getVulnerability().getLabel());
            table.addCell(String.valueOf(r.getFr()));
            table.addCell(String.valueOf(r.getStatus()));
        }
        document.add(table);
        document.close();
        byte[] data = out.toByteArray();
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=risk-results.pdf").contentType(MediaType.APPLICATION_PDF).body(new ByteArrayResource(data));
    }
}