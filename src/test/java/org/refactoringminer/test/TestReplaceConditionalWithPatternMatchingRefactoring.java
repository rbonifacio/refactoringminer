package org.refactoringminer.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import gr.uom.java.xmi.UMLModel;
import gr.uom.java.xmi.UMLModelASTReader;
import gr.uom.java.xmi.diff.UMLModelDiff;

public class TestReplaceConditionalWithPatternMatchingRefactoring {

    private static final String BASE = System.getProperty("user.dir")
            + "/src/test/resources/python/replace-conditional-with-pattern-matching/";

    private List<Refactoring> detect(String scenarioDir, String relFilePath) throws Exception {
        File beforeFile = new File(BASE + scenarioDir + "/before/" + relFilePath);
        File afterFile  = new File(BASE + scenarioDir + "/after/"  + relFilePath);
        String beforeContent = new String(Files.readAllBytes(beforeFile.toPath()));
        String afterContent  = new String(Files.readAllBytes(afterFile.toPath()));

        Map<String, String> filesBefore = new LinkedHashMap<>();
        Map<String, String> filesAfter  = new LinkedHashMap<>();
        filesBefore.put(relFilePath, beforeContent);
        filesAfter.put(relFilePath, afterContent);

        UMLModel modelBefore = new UMLModelASTReader(filesBefore, Collections.emptySet(), false).getUmlModel();
        UMLModel modelAfter  = new UMLModelASTReader(filesAfter,  Collections.emptySet(), false).getUmlModel();

        UMLModelDiff diff = modelBefore.diff(modelAfter);
        return diff.getRefactorings();
    }

    /**
     * Scenario 1: if/elif/else dispatching on string -> match/case
     * Expected: exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testSimpleIfElifElseReplacedByMatch() throws Exception {
        List<Refactoring> refactorings = detect("scenario1", "shape.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING");
    }

    /**
     * Scenario 2: four elif branches dispatching on integer status code -> match/case
     * Expected: exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testMultipleElifBranchesReplacedByMatch() throws Exception {
        List<Refactoring> refactorings = detect("scenario2", "http.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING");
    }

    /**
     * Scenario 3: if/elif -> match/case with guard clauses (case x if x < 0:)
     * Expected: exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING (guards do not cause extra detections)
     */
    @Test
    public void testMatchWithGuardClauseNoFalsePositives() throws Exception {
        List<Refactoring> refactorings = detect("scenario3", "guard.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING (guard clauses must not produce extra detections)");
    }

    /**
     * No refactoring when before and after are identical (scenario1/before used for both).
     * Expected: 0 REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING
     */
    @Test
    public void testNoRefactoringWhenConditionalUnchanged() throws Exception {
        File beforeFile = new File(BASE + "scenario1/before/shape.py");
        String content = new String(Files.readAllBytes(beforeFile.toPath()));

        Map<String, String> files = new LinkedHashMap<>();
        files.put("shape.py", content);

        UMLModel model = new UMLModelASTReader(files, Collections.emptySet(), false).getUmlModel();
        UMLModelDiff diff = model.diff(model);
        List<Refactoring> refactorings = diff.getRefactorings();

        long count = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING)
                .count();

        assertEquals(0, count, "Expected no REPLACE_CONDITIONAL_WITH_PATTERN_MATCHING when files are identical");
    }
}
