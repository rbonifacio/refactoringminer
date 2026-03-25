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

public class TestRemoveVariableTypeAnnotationRefactoring {

    private static final String BASE = System.getProperty("user.dir")
            + "/src/test/resources/python/remove-variable-type-annotation/";

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
     * Scenario 1: class attribute x: int = 0 -> x = 0
     * Expected: 1 REMOVE_VARIABLE_TYPE_ANNOTATION for attribute x
     */
    @Test
    public void testClassAttributeAnnotationRemoved() throws Exception {
        List<Refactoring> refactorings = detect("scenario1", "Shape.py");

        List<Refactoring> removeRefs = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REMOVE_VARIABLE_TYPE_ANNOTATION)
                .collect(Collectors.toList());

        assertEquals(1, removeRefs.size(), "Expected 1 REMOVE_VARIABLE_TYPE_ANNOTATION refactoring");
        String desc = removeRefs.get(0).toString();
        assertEquals(true, desc.contains("in attribute x"), "Description should mention 'in attribute x', got: " + desc);
        assertEquals(true, desc.contains("from class Shape"), "Description should mention 'from class Shape', got: " + desc);
    }

    /**
     * Scenario 2: local variable count: int = 0 -> count = 0 inside method process
     * Expected: 1 REMOVE_VARIABLE_TYPE_ANNOTATION for local variable count
     */
    @Test
    public void testLocalVariableAnnotationRemoved() throws Exception {
        List<Refactoring> refactorings = detect("scenario2", "DataProcessor.py");

        List<Refactoring> removeRefs = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REMOVE_VARIABLE_TYPE_ANNOTATION)
                .collect(Collectors.toList());

        assertEquals(1, removeRefs.size(), "Expected 1 REMOVE_VARIABLE_TYPE_ANNOTATION refactoring");
        String desc = removeRefs.get(0).toString();
        assertEquals(true, desc.contains("in variable count"), "Description should mention 'in variable count', got: " + desc);
        assertEquals(true, desc.contains("from class DataProcessor"), "Description should mention 'from class DataProcessor', got: " + desc);
    }

    /**
     * Scenario 3: class attribute items: list[str] = [] -> items = []
     * Expected: 1 REMOVE_VARIABLE_TYPE_ANNOTATION with removed type list[str]
     */
    @Test
    public void testComplexTypeAnnotationRemoved() throws Exception {
        List<Refactoring> refactorings = detect("scenario3", "Container.py");

        List<Refactoring> removeRefs = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REMOVE_VARIABLE_TYPE_ANNOTATION)
                .collect(Collectors.toList());

        assertEquals(1, removeRefs.size(), "Expected 1 REMOVE_VARIABLE_TYPE_ANNOTATION refactoring");
        String desc = removeRefs.get(0).toString();
        assertEquals(true, desc.contains("list[str]"), "Description should contain removed type 'list[str]', got: " + desc);
        assertEquals(true, desc.contains("in attribute items"), "Description should mention 'in attribute items', got: " + desc);
    }

    /**
     * Scenario 4: unchanged file — both attribute and local variable remain annotated
     * Expected: 0 REMOVE_VARIABLE_TYPE_ANNOTATION refactorings
     */
    @Test
    public void testNoFalsePositivesWhenAnnotationsUnchanged() throws Exception {
        List<Refactoring> refactorings = detect("scenario4", "Unchanged.py");

        long count = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.REMOVE_VARIABLE_TYPE_ANNOTATION)
                .count();

        assertEquals(0, count, "Expected 0 REMOVE_VARIABLE_TYPE_ANNOTATION refactorings for unchanged file");
    }
}
