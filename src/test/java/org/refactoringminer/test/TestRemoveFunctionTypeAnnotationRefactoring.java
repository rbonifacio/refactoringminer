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

public class TestRemoveFunctionTypeAnnotationRefactoring {

    private static final String BASE = System.getProperty("user.dir")
            + "/src/test/resources/python/remove-function-type-annotation/";

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
     * Scenario 1: def add(self, x: int, y: int) -> int: -> def add(self, x, y):
     * Expected: 2 REMOVE_PARAMETER_TYPE_ANNOTATION + 1 REMOVE_RETURN_TYPE_ANNOTATION
     */
    @Test
    public void testAllAnnotationsRemoved() throws Exception {
        List<Refactoring> refactorings = detect("scenario1", "Calculator.py");

        long paramRefs = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REMOVE_PARAMETER_TYPE_ANNOTATION)
                .count();
        long returnRefs = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REMOVE_RETURN_TYPE_ANNOTATION)
                .count();

        assertEquals(2, paramRefs, "Expected 2 REMOVE_PARAMETER_TYPE_ANNOTATION refactorings");
        assertEquals(1, returnRefs, "Expected 1 REMOVE_RETURN_TYPE_ANNOTATION refactoring");
    }

    /**
     * Scenario 2: def process(self, items: list[int]): -> def process(self, items):
     * Expected: 1 REMOVE_PARAMETER_TYPE_ANNOTATION with type list[int]
     */
    @Test
    public void testComplexParameterAnnotationRemoved() throws Exception {
        List<Refactoring> refactorings = detect("scenario2", "Processor.py");

        List<Refactoring> paramRefs = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REMOVE_PARAMETER_TYPE_ANNOTATION)
                .collect(Collectors.toList());

        assertEquals(1, paramRefs.size(), "Expected 1 REMOVE_PARAMETER_TYPE_ANNOTATION refactoring");
        String desc = paramRefs.get(0).toString();
        assertEquals(true, desc.contains("list[int]"), "Description should contain removed type 'list[int]', got: " + desc);
        assertEquals(true, desc.contains("in parameter items"), "Description should mention 'in parameter items', got: " + desc);
    }

    /**
     * Scenario 3: def get(self) -> str: -> def get(self):
     * Expected: 1 REMOVE_RETURN_TYPE_ANNOTATION with type str
     */
    @Test
    public void testReturnTypeAnnotationRemoved() throws Exception {
        List<Refactoring> refactorings = detect("scenario3", "Getter.py");

        List<Refactoring> returnRefs = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REMOVE_RETURN_TYPE_ANNOTATION)
                .collect(Collectors.toList());

        assertEquals(1, returnRefs.size(), "Expected 1 REMOVE_RETURN_TYPE_ANNOTATION refactoring");
        String desc = returnRefs.get(0).toString();
        assertEquals(true, desc.contains("str"), "Description should contain removed type 'str', got: " + desc);
    }

    /**
     * Scenario 4: unchanged file — no annotations present in either version
     * Expected: 0 removal refactorings
     */
    @Test
    public void testNoFalsePositivesWhenAlreadyUnannotated() throws Exception {
        List<Refactoring> refactorings = detect("scenario4", "NoChange.py");

        long paramCount = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REMOVE_PARAMETER_TYPE_ANNOTATION)
                .count();
        long returnCount = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REMOVE_RETURN_TYPE_ANNOTATION)
                .count();

        assertEquals(0, paramCount, "Expected 0 REMOVE_PARAMETER_TYPE_ANNOTATION refactorings");
        assertEquals(0, returnCount, "Expected 0 REMOVE_RETURN_TYPE_ANNOTATION refactorings");
    }
}
