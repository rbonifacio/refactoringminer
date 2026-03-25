package gr.uom.java.xmi.diff;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.refactoringminer.api.RefactoringType;

import gr.uom.java.xmi.UMLOperation;

public class ChangeTypeAnnotationRefactoring implements MethodLevelRefactoring {
	private final RefactoringType type;
	private final String typeBefore;
	private final String typeAfter;
	private final String parameterName;
	private final UMLOperation operationBefore;
	private final UMLOperation operationAfter;

	/**
	 * For CHANGE_PARAMETER_TYPE_ANNOTATION: parameterName is the name of the annotated parameter.
	 * For CHANGE_RETURN_TYPE_ANNOTATION: parameterName should be null.
	 */
	public ChangeTypeAnnotationRefactoring(RefactoringType type, String typeBefore, String typeAfter,
			String parameterName, UMLOperation operationBefore, UMLOperation operationAfter) {
		this.type = type;
		this.typeBefore = typeBefore;
		this.typeAfter = typeAfter;
		this.parameterName = parameterName;
		this.operationBefore = operationBefore;
		this.operationAfter = operationAfter;
	}

	@Override
	public RefactoringType getRefactoringType() {
		return type;
	}

	@Override
	public String getName() {
		return type.getDisplayName();
	}

	public String getTypeBefore() {
		return typeBefore;
	}

	public String getTypeAfter() {
		return typeAfter;
	}

	public String getParameterName() {
		return parameterName;
	}

	@Override
	public UMLOperation getOperationBefore() {
		return operationBefore;
	}

	@Override
	public UMLOperation getOperationAfter() {
		return operationAfter;
	}

	@Override
	public List<CodeRange> leftSide() {
		List<CodeRange> ranges = new ArrayList<>();
		ranges.add(operationBefore.codeRange()
				.setDescription("original method declaration")
				.setCodeElement(operationBefore.toString()));
		return ranges;
	}

	@Override
	public List<CodeRange> rightSide() {
		List<CodeRange> ranges = new ArrayList<>();
		ranges.add(operationAfter.codeRange()
				.setDescription("method declaration with changed type annotation")
				.setCodeElement(operationAfter.toString()));
		return ranges;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName()).append("\t");
		sb.append(typeBefore).append(" to ").append(typeAfter);
		if (type == RefactoringType.CHANGE_PARAMETER_TYPE_ANNOTATION) {
			sb.append(" in parameter ").append(parameterName);
		}
		sb.append(" in method ").append(operationAfter.toQualifiedString());
		sb.append(" from class ").append(operationAfter.getClassName());
		return sb.toString();
	}

	@Override
	public Set<ImmutablePair<String, String>> getInvolvedClassesBeforeRefactoring() {
		Set<ImmutablePair<String, String>> pairs = new LinkedHashSet<>();
		pairs.add(new ImmutablePair<>(operationBefore.getLocationInfo().getFilePath(), operationBefore.getClassName()));
		return pairs;
	}

	@Override
	public Set<ImmutablePair<String, String>> getInvolvedClassesAfterRefactoring() {
		Set<ImmutablePair<String, String>> pairs = new LinkedHashSet<>();
		pairs.add(new ImmutablePair<>(operationAfter.getLocationInfo().getFilePath(), operationAfter.getClassName()));
		return pairs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((typeBefore == null) ? 0 : typeBefore.hashCode());
		result = prime * result + ((typeAfter == null) ? 0 : typeAfter.hashCode());
		result = prime * result + ((parameterName == null) ? 0 : parameterName.hashCode());
		result = prime * result + ((operationAfter == null) ? 0 : operationAfter.hashCode());
		result = prime * result + ((operationBefore == null) ? 0 : operationBefore.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		ChangeTypeAnnotationRefactoring other = (ChangeTypeAnnotationRefactoring) obj;
		if (type != other.type) return false;
		if (typeBefore == null ? other.typeBefore != null : !typeBefore.equals(other.typeBefore)) return false;
		if (typeAfter == null ? other.typeAfter != null : !typeAfter.equals(other.typeAfter)) return false;
		if (parameterName == null ? other.parameterName != null : !parameterName.equals(other.parameterName)) return false;
		if (operationAfter == null ? other.operationAfter != null : !operationAfter.equals(other.operationAfter)) return false;
		if (operationBefore == null ? other.operationBefore != null : !operationBefore.equals(other.operationBefore)) return false;
		return true;
	}
}
