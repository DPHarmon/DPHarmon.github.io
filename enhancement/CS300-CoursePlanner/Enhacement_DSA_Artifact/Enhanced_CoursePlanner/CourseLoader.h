#ifndef COURSE_LOADER_H
#define COURSE_LOADER_H

#include "AVLTree.h"
#include "Course.h"
#include <string>
#include <vector>

/*
 * CourseLoader.h
 * ----------------------------------------------------------------------------
 * Public interface for the data-access layer. Reads the course CSV in a
 * single pass, validates prerequisites against an unordered_set of known
 * course IDs, and inserts valid courses into an AVLTree. Replaces the
 * original QuickSort + BinarySearch two-pass approach with O(1) lookup.
 *
 * Date: 07/20/2026
 * Author: Dylan Harmon
 */

class CourseLoader {
public:
	struct LoadResult {
		bool success = false;
		int coursesLoaded = 0;
		std::vector<std::string> errors;
	};

	/*
	*	Reads filepath once, validates prerequisites, and inserts
	*	valid courses into the tree. A course is skipped (with a warning)
	*	if any of its prerequisites do not correspond to a course
	*	present in the file.
	*/
	LoadResult loadInto(const std::string& filePath, AVLTree& tree) const;

private:
	static std::vector<std::string> SplitCsvLine(const std::string& line);
	static std::string Trim(const std::string& s);
};


#endif // !COURSE_LOADER_H

