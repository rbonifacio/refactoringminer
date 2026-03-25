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

public class TestChangeFunctionTypeAnnotationRefactoring {

    private static final String BASE = System.getProperty("user.dir")
            + "/src/test/resources/python/change-function-type-annotation/";

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
     * Scenario 1: def add(self, x: List[int], y: List[int]) -> List[int]: -> def add(self, x: list[int], y: list[int]) -> list[int]:
     * Expected: 2 CHANGE_PARAMETER_TYPE_ANNOTATION + 1 CHANGE_RETURN_TYPE_ANNOTATION
     */
    @Test
    public void testParameterAnnotationChanged() throws Exception {
        List<Refactoring> refactorings = detect("scenario1", "Calculator.py");

        long paramRefs = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.CHANGE_PARAMETER_TYPE_ANNOTATION)
                .count();
        long returnRefs = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.CHANGE_RETURN_TYPE_ANNOTATION)
                .count();

        assertEquals(2, paramRefs, "Expected 2 CHANGE_PARAMETER_TYPE_ANNOTATION refactorings");
        assertEquals(1, returnRefs, "Expected 1 CHANGE_RETURN_TYPE_ANNOTATION refactoring");
    }

    /**
     * Scenario 2: def process(self, items: Optional[str]): -> def process(self, items: str | None):
     * Expected: 1 CHANGE_PARAMETER_TYPE_ANNOTATION containing Optional[str] and str | None
     */
    @Test
    public void testParameterAnnotationChangedToUnion() throws Exception {
        List<Refactoring> refactorings = detect("scenario2", "Processor.py");

        List<Refactoring> paramRefs = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.CHANGE_PARAMETER_TYPE_ANNOTATION)
                .collect(Collectors.toList());

        assertEquals(1, paramRefs.size(), "Expected 1 CHANGE_PARAMETER_TYPE_ANNOTATION refactoring");
        String desc = paramRefs.get(0).toString();
        assertEquals(true, desc.contains("Optional[str]"), "Description should contain type before 'Optional[str]', got: " + desc);
        assertEquals(true, desc.contains("str|None"), "Description should contain type after 'str|None', got: " + desc);
        assertEquals(true, desc.contains("in parameter items"), "Description should mention 'in parameter items', got: " + desc);
    }

    /**
     * Scenario 3: def get(self) -> List[str]: -> def get(self) -> list[str]:
     * Expected: 1 CHANGE_RETURN_TYPE_ANNOTATION with before type List[str] and after type list[str]
     */
    @Test
    public void testReturnTypeAnnotationChanged() throws Exception {
        List<Refactoring> refactorings = detect("scenario3", "Getter.py");

        List<Refactoring> returnRefs = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.CHANGE_RETURN_TYPE_ANNOTATION)
                .collect(Collectors.toList());

        assertEquals(1, returnRefs.size(), "Expected 1 CHANGE_RETURN_TYPE_ANNOTATION refactoring");
        String desc = returnRefs.get(0).toString();
        assertEquals(true, desc.contains("List[str]"), "Description should contain type before 'List[str]', got: " + desc);
        assertEquals(true, desc.contains("list[str]"), "Description should contain type after 'list[str]', got: " + desc);
    }

    /**
     * Scenario 4: unchanged file — no annotations in either version
     * Expected: 0 change refactorings
     */
    @Test
    public void testNoFalsePositivesWhenUnannotated() throws Exception {
        List<Refactoring> refactorings = detect("scenario4", "NoChange.py");

        long paramCount = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.CHANGE_PARAMETER_TYPE_ANNOTATION)
                .count();
        long returnCount = refactorings.stream()
                .filter(r -> r.getRefactoringType() == RefactoringType.CHANGE_RETURN_TYPE_ANNOTATION)
                .count();

        assertEquals(0, paramCount, "Expected 0 CHANGE_PARAMETER_TYPE_ANNOTATION refactorings");
        assertEquals(0, returnCount, "Expected 0 CHANGE_RETURN_TYPE_ANNOTATION refactorings");
    }
}
