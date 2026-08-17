#include "CourseLoader.h"

#include <fstream>
#include <sstream>
#include <unordered_set>

/*
 * CourseLoader.cpp
 * ----------------------------------------------------------------------------
 * Implementation of the CSV loader. Handles file I/O, comma-splitting,
 * whitespace trimming, hash-set-based prerequisite validation, and reports
 * error lines or unresolved prerequisites through the LoadResult.
 *
 * Date: 07/20/2026
 * Author: Dylan P Harmon
 */

CourseLoader::LoadResult
CourseLoader::loadInto(const std::string& filePath, AVLTree& tree) const {
	LoadResult result; 

	std::ifstream file(filePath);
	if (!file.is_open()) {
		result.errors.push_back("Unable to open file: " + filePath);
		return result;
	}

	/*
	*	Single Pass -> Build two things at once:
	*		- every course object, in file order
	*		- an unordered set of course numbers for
	*			prerequisite validation used later
	*/
	std::vector<Course> parsed;
	std::unordered_set<std::string> knownIds;

	std::string line;
	int lineNumber = 0;
	while (std::getline(file, line)) {
		++lineNumber;

		// Handle Line endings
		if (!line.empty() && line.back() == '\r') line.pop_back();
		if (line.empty()) continue;

		std::vector<std::string> tokens = SplitCsvLine(line);
		if (tokens.size() < 2) {
			std::ostringstream oss;
			oss << "Line " << lineNumber
				<< ": Format Error (needs at least course ID Number and Course Name)";
			result.errors.push_back(oss.str());
			continue;
		}

		Course course;
		course.courseNumber = Trim(tokens[0]);
		course.courseName = Trim(tokens[1]);
		for (std::size_t i = 2; i < tokens.size(); i++) {
			std::string prereq = Trim(tokens[i]);
			if (!prereq.empty()) {
				course.prerequisites.push_back(prereq);
			}
		}

		parsed.push_back(course);
		knownIds.insert(course.courseNumber);
	}

	/*
	*	Validate Prerequisites in memory using the hash set.
	*	A course is inserted into the tree only if every one of its
	*	prerequisites resolves to a course present in file.
	*/
	for (const Course& course : parsed) {
		bool valid = true;
		for (const std::string& prereq : course.prerequisites) {
			if (knownIds.find(prereq) == knownIds.end()) {
				std::ostringstream oss;
				oss << "Course	" << course.courseNumber
					<< " references unknown prerequisite: " << prereq;
				result.errors.push_back(oss.str());
				valid = false;
			}
		}
		if (valid) {
			tree.Insert(course);
			++result.coursesLoaded;
		}
	}

	result.success = (result.coursesLoaded > 0);
	return result;
}

/*
*	Simple Comma-Spliter
*/
std::vector<std::string> CourseLoader::SplitCsvLine(const std::string& line) {
	std::vector<std::string> tokens;
	std::stringstream ss(line);
	std::string token;
	while (std::getline(ss, token, ',')) {
		tokens.push_back(token);
	}
	return tokens;
}

/*
*	Trim - Strips the leading and trailing whitespace from a string
*/
std::string CourseLoader::Trim(const std::string& s) {
	// set of whitespace characters
	const std::string ws = " \t\r\n";

	// scans left to right - returns index of firs character that is not in ws
	std::size_t start = s.find_first_not_of(ws);	
	
	// npos is what is returned when the entire line is empty/whitespace
	if (start == std::string::npos) return "";
	
	// scans right to left: returns the index of the last non-whitespace character
	std::size_t end = s.find_last_not_of(ws);
	
	// Cleaned up string - no whitespace.
	return s.substr(start, end - start + 1);

}