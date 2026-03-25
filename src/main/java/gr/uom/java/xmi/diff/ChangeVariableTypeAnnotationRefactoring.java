package gr.uom.java.xmi.diff;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.refactoringminer.api.RefactoringType;

import gr.uom.java.xmi.VariableDeclarationContainer;
import gr.uom.java.xmi.decomposition.VariableDeclaration;

public class ChangeVariableTypeAnnotationRefactoring implements MethodLevelRefactoring {
	private VariableDeclaration variableBefore;
	private VariableDeclaration variableAfter;
	private VariableDeclarationContainer operationBefore;
	private VariableDeclarationContainer operationAfter;

	public ChangeVariableTypeAnnotationRefactoring(VariableDeclaration variableBefore, VariableDeclaration variableAfter,
			VariableDeclarationContainer operationBefore, VariableDeclarationContainer operationAfter) {
		this.variableBefore = variableBefore;
		this.variableAfter = variableAfter;
		this.operationBefore = operationBefore;
		this.operationAfter = operationAfter;
	}

	public VariableDeclaration getVariableBefore() {
		return variableBefore;
	}

	public VariableDeclaration getVariableAfter() {
		return variableAfter;
	}

	public VariableDeclarationContainer getOperationBefore() {
		return operationBefore;
	}

	public VariableDeclarationContainer getOperationAfter() {
		return operationAfter;
	}

	@Override
	public RefactoringType getRefactoringType() {
		return RefactoringType.CHANGE_VARIABLE_TYPE_ANNOTATION;
	}

	@Override
	public String getName() {
		return this.getRefactoringType().getDisplayName();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName()).append("\t");
		sb.append(variableBefore.getType().toString());
		sb.append(" to ");
		sb.append(variableAfter.getType().toString());
		if (variableAfter.isAttribute()) {
			sb.append(" in attribute ");
			sb.append(variableAfter.getVariableName());
			sb.append(" from class ");
			sb.append(operationAfter.getClassName());
		} else {
			sb.append(" in variable ");
			sb.append(variableAfter.getVariableName());
			String elementType = operationAfter.getElementType();
			sb.append(" in " + elementType + " ");
			sb.append(operationAfter.toQualifiedString());
			sb.append(" from class ");
			sb.append(operationAfter.getClassName());
		}
		return sb.toString();
	}

	@Override
	public List<CodeRange> leftSide() {
		List<CodeRange> ranges = new ArrayList<>();
		ranges.add(variableBefore.codeRange()
				.setDescription("original variable declaration")
				.setCodeElement(variableBefore.toString()));
		String elementType = operationBefore.getElementType();
		ranges.add(operationBefore.codeRange()
				.setDescription("original " + elementType + " declaration")
				.setCodeElement(operationBefore.toString()));
		return ranges;
	}

	@Override
	public List<CodeRange> rightSide() {
		List<CodeRange> ranges = new ArrayList<>();
		ranges.add(variableAfter.codeRange()
				.setDescription("variable declaration with changed type annotation")
				.setCodeElement(variableAfter.toString()));
		String elementType = operationAfter.getElementType();
		ranges.add(operationAfter.codeRange()
				.setDescription(elementType + " declaration with changed variable type annotation")
				.setCodeElement(operationAfter.toString()));
		return ranges;
	}

	@Override
	public Set<ImmutablePair<String, String>> getInvolvedClassesBeforeRefactoring() {
		Set<ImmutablePair<String, String>> pairs = new LinkedHashSet<>();
		pairs.add(new ImmutablePair<>(
				getOperationBefore().getLocationInfo().getFilePath(),
				getOperationBefore().getClassName()));
		return pairs;
	}

	@Override
	public Set<ImmutablePair<String, String>> getInvolvedClassesAfterRefactoring() {
		Set<ImmutablePair<String, String>> pairs = new LinkedHashSet<>();
		pairs.add(new ImmutablePair<>(
				getOperationAfter().getLocationInfo().getFilePath(),
				getOperationAfter().getClassName()));
		return pairs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((operationAfter == null) ? 0 : operationAfter.hashCode());
		result = prime * result + ((operationBefore == null) ? 0 : operationBefore.hashCode());
		result = prime * result + ((variableAfter == null) ? 0 : variableAfter.hashCode());
		result = prime * result + ((variableBefore == null) ? 0 : variableBefore.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ChangeVariableTypeAnnotationRefactoring other = (ChangeVariableTypeAnnotationRefactoring) obj;
		if (operationAfter == null) {
			if (other.operationAfter != null) return false;
		} else if (!operationAfter.equals(other.operationAfter)) return false;
		if (operationBefore == null) {
			if (other.operationBefore != null) return false;
		} else if (!operationBefore.equals(other.operationBefore)) return false;
		if (variableAfter == null) {
			if (other.variableAfter != null) return false;
		} else if (!variableAfter.equals(other.variableAfter)) return false;
		if (variableBefore == null) {
			if (other.variableBefore != null) return false;
		} else if (!variableBefore.equals(other.variableBefore)) return false;
		return true;
	}
}
