package com.aby.airesume.controller;

import com.aby.airesume.service.MatchingService;
import com.aby.airesume.util.cleaner;
import com.aby.airesume.util.PDFReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
public class ResumeController {

    @Autowired
    private MatchingService matchingService;

    @GetMapping("/")
    public String index() {
        return "index"; // loads index.html
    }

    @PostMapping("/match")
    public String match(
            @RequestParam("jobDescription") String jobDescription,
            @RequestParam(value = "resumeText", required = false) String resumeText,
            @RequestParam(value = "resumeFile", required = false) MultipartFile resumeFile,
            Model model) throws IOException {

        String jdClean = cleaner.clean(jobDescription);

        String resumeContent = "";

        // If PDF uploaded → extract text
        if (resumeFile != null && !resumeFile.isEmpty()) {
            File temp = File.createTempFile("upload-", ".pdf");
            resumeFile.transferTo(temp);

            resumeContent = PDFReader.extractText(temp.getAbsolutePath());
            temp.delete();
        }
        // If text area used → take text
        else if (resumeText != null) {
            resumeContent = resumeText;
        }

        // Clean resume text
        String resumeClean = cleaner.clean(resumeContent);

        // Perform match
        MatchingService.MatchResult result = matchingService.match(jdClean, resumeClean);

        // Convert score to integer percentage
        int matchScore = (int) Math.round(result.getScore());

        // SEND DATA TO result.html (IMPORTANT)
        model.addAttribute("matchScore", matchScore); // ★ REQUIRED FOR SCORE CIRCLE
        model.addAttribute("jobDescription", jobDescription);
        model.addAttribute("resumeText", resumeContent); // ★ REQUIRED FOR RESUME BOX
        model.addAttribute("matched", result.getMatchedSkills());
        model.addAttribute("missing", result.getMissingSkills());
        model.addAttribute("decision", result.getDecision());

        return "result"; // loads result.html
    }
}
