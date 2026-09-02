"""
grade_analyzer.py

A small utility for analyzing a set of students' grades.
Given a list of students (each with a name and a list of numeric grades),
this script computes each student's average, median, and letter grade,
and reports the class average.
"""

from dataclasses import dataclass, field
from typing import List


@dataclass
class Student:
    name: str
    grades: List[float] = field(default_factory=list)

    def average(self) -> float:
        """Return the average of this student's grades."""
        if not self.grades:
            return 0.0
        return sum(self.grades) / len(self.grades)

    def median(self) -> float:
        """Return the median of this student's grades."""
        if not self.grades:
            return 0.0
        sorted_grades = sorted(self.grades)
        n = len(sorted_grades)
        mid = n // 2
        if n % 2 == 0:
            return (sorted_grades[mid - 1] + sorted_grades[mid]) / 2
        return sorted_grades[mid]

    def letter_grade(self) -> str:
        """Convert this student's average into a letter grade."""
        avg = self.average()
        if avg >= 90:
            return "A"
        elif avg >= 80:
            return "B"
        elif avg >= 70:
            return "C"
        elif avg >= 60:
            return "D"
        else:
            return "F"


def class_average(students: List[Student]) -> float:
    """Return the average of all students' averages."""
    if not students:
        return 0.0
    return sum(s.average() for s in students) / len(students)


def top_student(students: List[Student]) -> Student:
    """Return the student with the highest average. Raises if list is empty."""
    if not students:
        raise ValueError("Cannot find top student of an empty list")
    return max(students, key=lambda s: s.average())


def main():
    students = [
        Student("Aditi", [95, 88, 92]),
        Student("Sam", [70, 75, 68]),
        Student("Jordan", [82, 85, 79, 91]),
    ]

    for s in students:
        print(f"{s.name}: avg={s.average():.2f}, median={s.median():.2f}, "
              f"letter={s.letter_grade()}")

    print(f"\nClass average: {class_average(students):.2f}")
    print(f"Top student: {top_student(students).name}")


if __name__ == "__main__":
    main()
