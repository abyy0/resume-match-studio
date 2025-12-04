package com.aby.airesume.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    public static class MatchResult {
        private final double score;
        private final List<String> matchedSkills;
        private final List<String> missingSkills;
        private final String decision;

        public MatchResult(double score, List<String> matchedSkills, List<String> missingSkills, String decision) {
            this.score = score;
            this.matchedSkills = matchedSkills;
            this.missingSkills = missingSkills;
            this.decision = decision;
        }

        public double getScore() {
            return score;
        }

        public List<String> getMatchedSkills() {
            return matchedSkills;
        }

        public List<String> getMissingSkills() {
            return missingSkills;
        }

        public String getDecision() {
            return decision;
        }
    }

    // WEIGHTED SKILL IMPORTANCE
    private static final Map<String, Integer> PRIORITY_MAP = new HashMap<>();
    static {
        PRIORITY_MAP.put("java", 5);
        PRIORITY_MAP.put("python", 5);
        PRIORITY_MAP.put("spring", 5);
        PRIORITY_MAP.put("spring boot", 5);
        PRIORITY_MAP.put("react", 5);
        PRIORITY_MAP.put("node", 4);
        PRIORITY_MAP.put("mysql", 4);
        PRIORITY_MAP.put("mongodb", 4);
        PRIORITY_MAP.put("docker", 4);
        PRIORITY_MAP.put("kubernetes", 5);
        PRIORITY_MAP.put("aws", 5);

        PRIORITY_MAP.put("html", 2);
        PRIORITY_MAP.put("css", 2);
        PRIORITY_MAP.put("communication", 1);
        PRIORITY_MAP.put("teamwork", 1);
    }

    // MAIN MATCHER
    public MatchResult match(String jobDescription, String resumeText) {

        if (jobDescription == null)
            jobDescription = "";
        if (resumeText == null)
            resumeText = "";

        jobDescription = jobDescription.toLowerCase();
        resumeText = resumeText.toLowerCase();

        // Extract keywords from both
        List<String> jdSkills = extractKeywords(jobDescription);
        List<String> resumeSkills = extractKeywords(resumeText);

        // Determine matches
        List<String> matched = jdSkills.stream()
                .filter(resumeSkills::contains)
                .distinct()
                .collect(Collectors.toList());

        List<String> missing = jdSkills.stream()
                .filter(skill -> !matched.contains(skill))
                .distinct()
                .collect(Collectors.toList());

        // Calculate weighted score
        double score = calculateWeightedScore(jdSkills, matched);

        // Experience detection
        score += detectExperienceBoost(resumeText);

        // Achievement-based bonus
        score += detectAchievementBoost(resumeText);

        // Keep score clean
        if (score < 0)
            score = 0;
        if (score > 100)
            score = 100;

        // Decision
        String decision;
        if (score >= 85)
            decision = "Excellent Match";
        else if (score >= 65)
            decision = "Good Match";
        else if (score >= 40)
            decision = "Average Match";
        else
            decision = "Weak Match";

        return new MatchResult(score, matched, missing, decision);
    }

    // ===============================
    // WEIGHTED SCORE LOGIC
    // ===============================
    private double calculateWeightedScore(List<String> jdSkills, List<String> matched) {

        int totalWeight = 0;
        int matchedWeight = 0;

        for (String skill : jdSkills) {
            int weight = PRIORITY_MAP.getOrDefault(skill, 2); // default weight
            totalWeight += weight;

            if (matched.contains(skill)) {
                matchedWeight += weight;
            }
        }

        if (totalWeight == 0)
            return 0;

        return ((double) matchedWeight / totalWeight) * 80; // 80% score from skills
    }

    // ===============================
    // EXPERIENCE BOOST
    // ===============================
    private double detectExperienceBoost(String resumeText) {
        if (resumeText.contains("years") || resumeText.matches(".*\\b[3-9]\\+? years?.*")) {
            return 10; // bonus for strong experience
        }
        if (resumeText.contains("intern") || resumeText.contains("project")) {
            return 5;
        }
        return 0;
    }

    // ===============================
    // ACHIEVEMENT PHRASE BONUS
    // ===============================
    private double detectAchievementBoost(String text) {
        String[] achievements = {
                "improved", "reduced", "implemented", "designed",
                "increased", "optimized", "built", "created",
                "automated", "launched"
        };

        for (String keyword : achievements) {
            if (text.contains(keyword)) {
                return 10; // strong bonus
            }
        }
        return 0;
    }

    // ===============================
    // KEYWORD EXTRACTION
    // ===============================
    private List<String> extractKeywords(String text) {

        text = text.replaceAll("[^a-zA-Z0-9 ]", " ");

        Set<String> stopwords = new HashSet<>(Arrays.asList(
                "the", "and", "or", "with", "in", "on", "at", "as", "to", "for",
                "is", "are", "of", "a", "an", "be", "have", "has", "that", "this",
                "by", "from", "we", "you", "your", "can", "will", "about"));

        List<String> words = Arrays.stream(text.split("\\s+"))
                .map(String::trim)
                .filter(w -> w.length() > 2)
                .filter(w -> !stopwords.contains(w))
                .collect(Collectors.toList());

        return words;
    }
}
