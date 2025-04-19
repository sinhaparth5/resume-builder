package com.example.resume_builder.controller;

import com.example.resume_builder.model.Resume;
import com.example.resume_builder.repository.ResumeRepository;
import com.example.resume_builder.service.TextEnhancerService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;

@Controller
public class ResumeController {
    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private TextEnhancerService textEnhancerService;

    @GetMapping("/")
    public String showInputForm() {
        return "input";
    }

    @PostMapping("/api/resume")
    public String createResume(@RequestParam(name = "name") String name, @RequestParam(name = "email") String email,
                               @RequestParam(name = "education") String education, @RequestParam(name = "experience") String experience,
                               @RequestParam(name = "skills") String skills, @RequestParam(name = "publications") String publications,
                               @RequestParam(name = "awards") String awards, @RequestParam(name = "extraTitle") String extraTitle,
                               @RequestParam(name = "extraPoints") String extraPoints) {
        Resume resume = new Resume();
        resume.setName(Encode.forHtml(name));
        resume.setEmail(Encode.forHtml(email));
        resume.setEducation(textEnhancerService.enhanceText(Encode.forHtml(education)));
        resume.setExperience(textEnhancerService.enhanceText(Encode.forHtml(experience)));
        resume.setSkills(textEnhancerService.enhanceText(Encode.forHtml(skills)));
        resume.setPublications(textEnhancerService.enhanceText(Encode.forHtml(publications)));
        resume.setAwards(textEnhancerService.enhanceText(Encode.forHtml(awards)));
        resume.setExtraTitle(Encode.forHtml(extraTitle));
        resume.setExtraPoints(textEnhancerService.enhanceText(Encode.forHtml(extraPoints)));
        resumeRepository.save(resume);
        return "redirect:/resume/preview/" + resume.getId();
    }

    @GetMapping("/resume/preview/{id}")
    public String previewResume(@PathVariable String id, Model model) {
        Resume resume = resumeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Resume not found"));
        model.addAttribute("resume", resume);
        return "preview";
    }

    @GetMapping("/api/resume/download")
    public ResponseEntity<byte[]> downloadResume(@RequestParam(name = "id") String id,
                                                 @RequestParam(name = "format") String format) throws Exception {
        Resume resume = resumeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Resume not found"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if ("pdf".equalsIgnoreCase(format)) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Create Harvard-style header
            document.add(new Paragraph(resume.getName()).setFontSize(16).setBold().setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(resume.getEmail()).setTextAlignment(TextAlignment.CENTER));

            // Education section with proper spacing
            document.add(new Paragraph("EDUCATION").setBold().setMarginTop(10));
            if (resume.getEducation() != null && !resume.getEducation().isEmpty()) {
                for (String point : resume.getEducation().split("\n")) {
                    document.add(new Paragraph("• " + point).setMarginLeft(15));
                }
            } else {
                document.add(new Paragraph("• Not provided").setMarginLeft(15));
            }

            // Experience section
            document.add(new Paragraph("EXPERIENCE").setBold().setMarginTop(10));
            if (resume.getExperience() != null && !resume.getExperience().isEmpty()) {
                for (String point : resume.getExperience().split("\n")) {
                    document.add(new Paragraph("• " + point).setMarginLeft(15));
                }
            } else {
                document.add(new Paragraph("• Not provided").setMarginLeft(15));
            }

            // Skills section
            document.add(new Paragraph("SKILLS").setBold().setMarginTop(10));
            if (resume.getSkills() != null && !resume.getSkills().isEmpty()) {
                for (String point : resume.getSkills().split("\n")) {
                    document.add(new Paragraph("• " + point).setMarginLeft(15));
                }
            } else {
                document.add(new Paragraph("• Not provided").setMarginLeft(15));
            }

            // Publications section
            document.add(new Paragraph("PUBLICATIONS").setBold().setMarginTop(10));
            if (resume.getPublications() != null && !resume.getPublications().isEmpty()) {
                for (String point : resume.getPublications().split("\n")) {
                    document.add(new Paragraph("• " + point).setMarginLeft(15));
                }
            } else {
                document.add(new Paragraph("• Not provided").setMarginLeft(15));
            }

            // Awards section
            document.add(new Paragraph("AWARDS").setBold().setMarginTop(10));
            if (resume.getAwards() != null && !resume.getAwards().isEmpty()) {
                for (String point : resume.getAwards().split("\n")) {
                    document.add(new Paragraph("• " + point).setMarginLeft(15));
                }
            } else {
                document.add(new Paragraph("• Not provided").setMarginLeft(15));
            }

            // Extra section (if provided)
            if (resume.getExtraTitle() != null && !resume.getExtraTitle().isEmpty()) {
                document.add(new Paragraph(resume.getExtraTitle().toUpperCase()).setBold().setMarginTop(10));
                if (resume.getExtraPoints() != null && !resume.getExtraPoints().isEmpty()) {
                    for (String point : resume.getExtraPoints().split("\n")) {
                        document.add(new Paragraph("• " + point).setMarginLeft(15));
                    }
                } else {
                    document.add(new Paragraph("• Not provided").setMarginLeft(15));
                }
            }

            document.close();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resume.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(baos.toByteArray());
        } else if ("docx".equalsIgnoreCase(format)) {
            XWPFDocument doc = new XWPFDocument();

            // Create Harvard-style header
            XWPFParagraph nameParagraph = doc.createParagraph();
            nameParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun nameRun = nameParagraph.createRun();
            nameRun.setText(resume.getName());
            nameRun.setBold(true);
            nameRun.setFontSize(16);

            XWPFParagraph emailParagraph = doc.createParagraph();
            emailParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun emailRun = emailParagraph.createRun();
            emailRun.setText(resume.getEmail());

            // Education section
            XWPFParagraph educationHeader = doc.createParagraph();
            XWPFRun educationRun = educationHeader.createRun();
            educationRun.setText("EDUCATION");
            educationRun.setBold(true);
            educationRun.addBreak();

            if (resume.getEducation() != null && !resume.getEducation().isEmpty()) {
                for (String point : resume.getEducation().split("\n")) {
                    XWPFParagraph p = doc.createParagraph();
                    p.setIndentationLeft(500); // Indent for bullet points
                    XWPFRun run = p.createRun();
                    run.setText("• " + point);
                }
            } else {
                XWPFParagraph p = doc.createParagraph();
                p.setIndentationLeft(500);
                XWPFRun run = p.createRun();
                run.setText("• Not provided");
            }

            // Experience section
            XWPFParagraph experienceHeader = doc.createParagraph();
            XWPFRun experienceRun = experienceHeader.createRun();
            experienceRun.setText("EXPERIENCE");
            experienceRun.setBold(true);
            experienceRun.addBreak();

            if (resume.getExperience() != null && !resume.getExperience().isEmpty()) {
                for (String point : resume.getExperience().split("\n")) {
                    XWPFParagraph p = doc.createParagraph();
                    p.setIndentationLeft(500);
                    XWPFRun run = p.createRun();
                    run.setText("• " + point);
                }
            } else {
                XWPFParagraph p = doc.createParagraph();
                p.setIndentationLeft(500);
                XWPFRun run = p.createRun();
                run.setText("• Not provided");
            }

            // Skills section
            XWPFParagraph skillsHeader = doc.createParagraph();
            XWPFRun skillsRun = skillsHeader.createRun();
            skillsRun.setText("SKILLS");
            skillsRun.setBold(true);
            skillsRun.addBreak();

            if (resume.getSkills() != null && !resume.getSkills().isEmpty()) {
                for (String point : resume.getSkills().split("\n")) {
                    XWPFParagraph p = doc.createParagraph();
                    p.setIndentationLeft(500);
                    XWPFRun run = p.createRun();
                    run.setText("• " + point);
                }
            } else {
                XWPFParagraph p = doc.createParagraph();
                p.setIndentationLeft(500);
                XWPFRun run = p.createRun();
                run.setText("• Not provided");
            }

            // Publications section
            XWPFParagraph publicationsHeader = doc.createParagraph();
            XWPFRun publicationsRun = publicationsHeader.createRun();
            publicationsRun.setText("PUBLICATIONS");
            publicationsRun.setBold(true);
            publicationsRun.addBreak();

            if (resume.getPublications() != null && !resume.getPublications().isEmpty()) {
                for (String point : resume.getPublications().split("\n")) {
                    XWPFParagraph p = doc.createParagraph();
                    p.setIndentationLeft(500);
                    XWPFRun run = p.createRun();
                    run.setText("• " + point);
                }
            } else {
                XWPFParagraph p = doc.createParagraph();
                p.setIndentationLeft(500);
                XWPFRun run = p.createRun();
                run.setText("• Not provided");
            }

            // Awards section
            XWPFParagraph awardsHeader = doc.createParagraph();
            XWPFRun awardsRun = awardsHeader.createRun();
            awardsRun.setText("AWARDS");
            awardsRun.setBold(true);
            awardsRun.addBreak();

            if (resume.getAwards() != null && !resume.getAwards().isEmpty()) {
                for (String point : resume.getAwards().split("\n")) {
                    XWPFParagraph p = doc.createParagraph();
                    p.setIndentationLeft(500);
                    XWPFRun run = p.createRun();
                    run.setText("• " + point);
                }
            } else {
                XWPFParagraph p = doc.createParagraph();
                p.setIndentationLeft(500);
                XWPFRun run = p.createRun();
                run.setText("• Not provided");
            }

            // Extra section (if provided)
            if (resume.getExtraTitle() != null && !resume.getExtraTitle().isEmpty()) {
                XWPFParagraph extraHeader = doc.createParagraph();
                XWPFRun extraRun = extraHeader.createRun();
                extraRun.setText(resume.getExtraTitle().toUpperCase());
                extraRun.setBold(true);
                extraRun.addBreak();

                if (resume.getExtraPoints() != null && !resume.getExtraPoints().isEmpty()) {
                    for (String point : resume.getExtraPoints().split("\n")) {
                        XWPFParagraph p = doc.createParagraph();
                        p.setIndentationLeft(500);
                        XWPFRun run = p.createRun();
                        run.setText("• " + point);
                    }
                } else {
                    XWPFParagraph p = doc.createParagraph();
                    p.setIndentationLeft(500);
                    XWPFRun run = p.createRun();
                    run.setText("• Not provided");
                }
            }

            doc.write(baos);
            doc.close();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resume.docx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(baos.toByteArray());
        }
        throw new IllegalArgumentException("Unsupported format: " + format);
    }
}
