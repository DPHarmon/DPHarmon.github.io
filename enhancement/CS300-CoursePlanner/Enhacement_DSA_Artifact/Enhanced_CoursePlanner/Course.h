#ifndef COURSE_H
#define COURSE_H

#include <string>
#include <vector>

/*
 * Course.h
 * ----------------------------------------------------------------------------
 * Domain model for the Course Planner. Defines the Course struct — the
 * shared vocabulary used by every layer of the application (data structure,
 * data access, and application). Contains no logic.
 *
 * Date: 07/19/2026
 * Author: Dylan Harmon
 */

struct Course {
	std::string courseNumber;
	std::string courseName;
	std::vector<std::string> prerequisites;
};

#endif