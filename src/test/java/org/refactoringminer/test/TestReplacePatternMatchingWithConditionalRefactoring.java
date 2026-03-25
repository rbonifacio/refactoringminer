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

public class TestReplacePatternMatchingWithConditionalRefactoring {

    private static final String BASE = System.getProperty("user.dir")
            + "/src/test/resources/python/replace-pattern-matching-with-conditional/";

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
     * Scenario 1: match/case dispatching on string -> if/elif/else
     * Expected: exactly 1 REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL
     */
    @Test
    public void testSimpleMatchReplacedByIfElifElse() throws Exception {
        List<Refactoring> refactorings = detect("scenario1", "shape.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL");
    }

    /**
     * Scenario 2: match/case with four branches dispatching on integer -> if/elif/else
     * Expected: exactly 1 REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL
     */
    @Test
    public void testMultipleCaseBranchesReplacedByElifChain() throws Exception {
        List<Refactoring> refactorings = detect("scenario2", "http.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL");
    }

    /**
     * Scenario 3: match/case with guard clauses -> if/elif
     * Expected: exactly 1 REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL
     */
    @Test
    public void testMatchWithGuardClauseReplacedByIfElif() throws Exception {
        List<Refactoring> refactorings = detect("scenario3", "guard.py");

        List<Refactoring> matches = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL)
                .collect(Collectors.toList());

        assertEquals(1, matches.size(), "Expected exactly 1 REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL");
    }

    /**
     * No refactoring when before and after are identical (scenario1/before used for both).
     * Expected: 0 REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL
     */
    @Test
    public void testNoRefactoringWhenMatchUnchanged() throws Exception {
        File beforeFile = new File(BASE + "scenario1/before/shape.py");
        String content = new String(Files.readAllBytes(beforeFile.toPath()));

        Map<String, String> files = new LinkedHashMap<>();
        files.put("shape.py", content);

        UMLModel model = new UMLModelASTReader(files, Collections.emptySet(), false).getUmlModel();
        UMLModelDiff diff = model.diff(model);
        List<Refactoring> refactorings = diff.getRefactorings();

        long count = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL)
                .count();

        assertEquals(0, count, "Expected no REPLACE_PATTERN_MATCHING_WITH_CONDITIONAL when files are identical");
    }
}
