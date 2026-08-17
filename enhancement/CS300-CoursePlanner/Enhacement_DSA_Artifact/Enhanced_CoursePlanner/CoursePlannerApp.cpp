#include "CoursePlannerApp.h"

#include <algorithm>
#include <cctype>
#include <iostream>
#include <limits>
#include <string>


/*
 * CoursePlannerApp.cpp
 * ----------------------------------------------------------------------------
 * Implementation of the menu-driven console interface: load, print all
 * courses in alphanumeric order, look up a single course with its
 * prerequisites, and exit. Handles invalid input and normalizes search
 * input to uppercase for case-insensitive lookup.
 *
 * Date: 07/20/2026
 * Author: Dylan P Harmon
 */

void CoursePlannerApp::Run() {
	
	std::cout << "Welcome to the Course Planner.\n\n";

	int choice = 0;
	while (choice != 9) {
		PrintMenu();

		if (!(std::cin >> choice)) {
			// input invalid, clear and try again
			std::cin.clear();
			std::cin.ignore(std::numeric_limits<std::streamsize>::max(), '\n');
			std::cout << "Invalid input. Please enter a number.\n\n";
			choice = 0;
			continue;
		}

		// Drop newline so subsequent getline() calls work
		std::cin.ignore(std::numeric_limits<std::streamsize>::max(), '\n');

		switch (choice) {
		case 1: HandleLoad();			break;
		case 2: HandlePrintAll();		break;
		case 3: HandlePrintCourse();	break;
		case 9:
			std::cout << "Thank you for using the course planner!\n";
			break;
		default:
			std::cout << choice << " is not a valid option. \n\n";
			break;
		}
	}
}

void CoursePlannerApp::PrintMenu() const {
	
	std::cout << " 1. Load Data Structure.\n"
		<< " 2. Print Course List.\n"
		<< " 3. Print A Single Course.\n"
		<< " 9. Exit Course Planner. \n\n"
		<< " How can I help today?	";
}

/*
*	Menu Option - 1: Loads CSV into the tree
*	Prompts for a file path, delegates parsing and validation to
*	CourseLoader, then reports the result.
*/
void CoursePlannerApp::HandleLoad() {
	
	std::cout << "Enter the file name to load: ";
	std::string filePath;
	std::getline(std::cin, filePath);

	/* Delegate to the data-access layer */
	CourseLoader::LoadResult result = loader_.loadInto(filePath, tree_);

	/* Records either a count, or a failure notice */
	if (result.coursesLoaded == 0) {
		std::cout << "No courses were loaded.\n";
	}
	else {
		std::cout << "Loaded " << result.coursesLoaded << " course(s).\n";
	}

	/* Per-issue warnings collected during the load process */
	for (const std::string& err : result.errors) {
		std::cout << " Warnings: " << err << "\n";
	}
	std::cout << "\n";

}

/*
*	Menu Option - 2: Print every course in alphanumeric order.
*	Traversal order is guaranteed by the AVL tree's in-order walk.
*	Therefore no sort step is required.
*/
void CoursePlannerApp::HandlePrintAll() const {
	
	/* Verify CSV was  loaded previously */
	if (tree_.Empty()) {
		std::cout << "Course list is empty. Load the data structure first (option 1).\n\n";
		return;
	}

	/*
	*	Pass a lambda to -> ForEachInOrder. The tree does the traversal.
	*/
	std::cout << "THere is a list of the current available courses:\n\n";
	tree_.ForEachInOrder([](const Course& c) {
		std::cout << c.courseNumber << ", " << c.courseName << "\n";
	});
	std::cout << "\n";

}

/*
*	Menu Option - 3: Look up a single course by ID.
*	Search input is normalized to uppercase.
*/
void CoursePlannerApp::HandlePrintCourse() const {
	
	if (tree_.Empty()) {
		std::cout << "Course list is empty. Load the data structure first (option 1).\n\n";
		return;
	}

	std::cout << "Which course are you interested in? ";
	std::string input;
	std::getline(std::cin, input);

	/*
	*	AVLTree::Find returnsa pointer into the tree, or 
	*	a nullptr if the course is not present. 
	*/
	const Course* course = tree_.Find(ToUpper(input));
	if (!course) {
		std::cout << "Course " << input << " not found.\n\n";
		return;
	}
	
	PrintCourse(*course);

}

/*
*	Formats a single Course for output: ID and Name on the first line,
*	followed by its prerequisites or "None" if there are none.
*	Separated from HandlePrintCourse so that formatting logic can be
*	reused.
*/
void CoursePlannerApp::PrintCourse(const Course& course) const {
	
	std::cout << course.courseNumber << ", " << course.courseName << "\n";
	std::cout << "Prerequisites: ";
	if (course.prerequisites.empty()) {
		std::cout << "None";
	}
	else {
		for (std::size_t i = 0; i < course.prerequisites.size(); i++) {
			std::cout << course.prerequisites[i];
			if (i + 1 < course.prerequisites.size()) std::cout << ", ";
		}
	}
	std::cout << "\n\n";
}

/*
*	Returns uppercase copy of the input string. Used to
*	normalize the user's search query. 
*/
std::string CoursePlannerApp::ToUpper(const std::string& s) {
	std::string result = s;
	std::transform(result.begin(), result.end(), result.begin(),
		[](unsigned char c) {
			return static_cast<char>(std::toupper(c));
		});

	return result;
}
