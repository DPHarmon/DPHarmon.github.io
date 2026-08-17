#include "AVLTree.h"
#include <algorithm>

/*
 * AVLTree.cpp
 * ----------------------------------------------------------------------------
 * Implementation of the AVL tree: BST insert with height updates and the four
 * rebalance cases (LL, RR, LR, RL), recursive find and in-order traversal,
 * and a post-order destructor.
 *
 * Date: 07/19/2026
 * Author: Dylan Harmon
 */

// AVL Constructor
AVLTree::AVLTree() : root_(nullptr) { }

// AVL Destructor
AVLTree::~AVLTree() {
	Destroy(root_);
}

/*			Interface			*/

void AVLTree::Insert(const Course& course) {
	root_ = InsertNode(root_, course);
}

const Course* AVLTree::Find(const std::string& courseNumber) const {
	Node* node = FindNode(root_, courseNumber);
	return node ? &node->course : nullptr;
}

void AVLTree::ForEachInOrder(const std::function<void(const Course&)>& visit) const {
	InOrder(root_, visit);
}

bool AVLTree::Empty() const {
	return root_ == nullptr;
}

/*		Height / Balance		*/

int AVLTree::Height(Node* node) const {
	return node ? node->height : 0;
}

int AVLTree::Balance(Node* node) const {
	return node ? Height(node->left) - Height(node->right) : 0;
}

void AVLTree::UpdateHeight(Node* node) {
	if (node) {
		node->height = 1 + std::max(Height(node->left), Height(node->right));
	}
}

/*			Rotations			
*	
*	Right Rotation:
*		y			x
*	   / \		   / \
*	  x   c  ---> a   y
*	 / \             / \
*   a   b           b   c
*/
AVLTree::Node* AVLTree::RotateRight(Node* y) {
	Node* x = y->left;
	Node* b = x->right;

	x->right = y;
	y->left = b;

	UpdateHeight(y);	// Update Lower Node First
	UpdateHeight(x);

	return(x);			// Return new subtree root
}

// Left rotation is the mirror of right rotation
AVLTree::Node* AVLTree::RotateLeft(Node* x) {
	Node* y = x->right;
	Node* b = y->left;

	y->left = x;
	x->right = b;

	UpdateHeight(x);
	UpdateHeight(y);

	return y;
}

/*		Recursive Helpers		*/

AVLTree::Node* AVLTree::InsertNode(Node* node, const Course& course) {
	// Standard BST insertion first
	if (!node) {
		return new Node(course);
	}

	if (course.courseNumber < node->course.courseNumber) {
		node->left = InsertNode(node->left, course);
	}
	else if (course.courseNumber > node->course.courseNumber) {
		node->right = InsertNode(node->right, course);
	}
	else {
		// Duplicate course - ignore
		return node;
	}

	// Update this node's height, then check whether it is unbalanced.
	UpdateHeight(node);
	int balance = Balance(node);

	// Left-Left: new key went into the left child's left subtree.
	if (balance > 1 && course.courseNumber < node->left->course.courseNumber) {
		return RotateRight(node);
	}
	// Right-Right: new key went into the right child's right subtree.
	if (balance < -1 && course.courseNumber > node->right->course.courseNumber) {
		return RotateLeft(node);
	}
	// Left-Right: new key went into the left child's right subtree.
	if (balance > 1 && course.courseNumber > node->left->course.courseNumber) {
		node->left = RotateLeft(node->left);
		return RotateRight(node);
	}
	// Right-Left: new key went into the right child's left subtree
	if (balance < -1 && course.courseNumber < node->right->course.courseNumber) {
		node->right = RotateRight(node->right);
		return RotateLeft(node);
	}

	return node;

}

/* Recursive Search - Standard BST Lookup -
*	Balancing occurs during Insertion.
*	Provides O(Log n) because the tree stays balanced
*	
*/
AVLTree::Node* AVLTree::FindNode(Node* node, const std::string& courseNumber) const {
	if (!node) return nullptr;
	if (courseNumber == node->course.courseNumber) return node;
	if (courseNumber < node->course.courseNumber) {
		return FindNode(node->left, courseNumber);
	}
	return FindNode(node->right, courseNumber);
}

/* Recursive in-order traversal: 
*	Visits left subtree, then* this node,
*	the the right subtree. In-order traversal is used to show courses in
*	alphanumeric order. 
* 
* Visit - callback used to keep tree decoupled from the iostream: the 
* caller decides what to do with each Course.
*/
void AVLTree::InOrder(Node* node,
	const std::function<void(const Course&)>& visit) const {
	if (!node) return;
	InOrder(node->left, visit);
	visit(node->course);
	InOrder(node->right, visit);
}

/*	Post-order teardown for the destructor:
*	Children are deleted before their parent,
*	as to not defer a freed pointer. Called Once
*	from ~AVLTree with the root - recursion handles the rest.
*/
void AVLTree::Destroy(Node* node) {
	if (!node) return;
	Destroy(node->left);
	Destroy(node->right);
	delete node;
}
