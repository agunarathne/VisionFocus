# BMAD Development Guidelines

## 🚨 CRITICAL RULE: No Commits Before Code Review

**When developing with BMAD support, ALL code changes MUST go through code review BEFORE committing to version control.**

### Why This Matters
- Ensures code quality and maintainability
- Catches potential bugs and architectural issues early
- Validates accessibility compliance (critical for VisionFocus)
- Maintains consistency with project standards
- Prevents technical debt accumulation

---

## Development Workflow with BMAD

### 1. Story Implementation
```bash
# New chat → Load DEV agent
*dev-story
```

### 2. ⚠️ MANDATORY CODE REVIEW (Before Any Commit)
```bash
# New chat → Load DEV agent  
*code-review
```

**STOP HERE - Do NOT proceed to commit until:**
- [ ] Code review is complete
- [ ] All identified issues are resolved
- [ ] Reviewer (LLM or human) has approved the changes

### 3. Commit Only After Approval
```bash
git add -A
git commit -m "Descriptive commit message"
git push
```

---

## Code Review Checklist

Before committing any BMAD-generated code, verify:

### Functional Requirements
- [ ] Story acceptance criteria fully implemented
- [ ] Edge cases handled appropriately
- [ ] Error handling in place

### Code Quality
- [ ] No hardcoded values (use constants or config)
- [ ] Proper naming conventions followed
- [ ] Code is DRY (Don't Repeat Yourself)
- [ ] Comments explain "why", not "what"

### Architecture Compliance
- [ ] Follows established patterns (MVVM, Repository, etc.)
- [ ] Dependency injection used correctly (Hilt)
- [ ] Proper separation of concerns

### Accessibility (VisionFocus-Specific)
- [ ] All interactive elements have `contentDescription`
- [ ] Touch targets are ≥48dp × 48dp
- [ ] TalkBack announcements are clear and concise
- [ ] Focus order is logical
- [ ] Follows `docs/AccessibilityGuidelines.md`

### Testing
- [ ] Unit tests pass: `.\gradlew.bat test`
- [ ] No new compilation errors
- [ ] Manual testing completed (if applicable)

### Documentation
- [ ] KDoc added for public APIs
- [ ] README updated (if feature affects usage)
- [ ] Implementation artifacts updated

---

## Recommended Review Process

### Option 1: LLM Code Review (Recommended for Solo Development)
1. Open a **new chat** (fresh context)
2. Load the DEV agent
3. Run: `*code-review`
4. Address all findings before committing

### Option 2: Peer Review (Recommended for Team Projects)
1. Create a feature branch
2. Push changes: `git push origin feature/story-x.x`
3. Create pull request
4. Wait for peer approval
5. Merge after approval

### Option 3: Self-Review (Minimum Acceptable)
1. Read through all changed files
2. Check against the checklist above
3. Test manually with TalkBack enabled
4. Document your review findings

---

## What NOT to Do

### ❌ PROHIBITED: Direct Commit Without Review
```bash
# WRONG - Never do this after BMAD development
git add -A
git commit -m "Quick fix"
git push
```

### ❌ PROHIBITED: Committing with Known Issues
- "I'll fix it later" → Schedule it, don't commit broken code
- "Just a small bug" → Small bugs compound into large problems
- "Only affects edge cases" → Accessibility users often hit edge cases

### ❌ PROHIBITED: Skipping Tests
- Always run unit tests before review
- Run instrumentation tests if UI changed
- Test with TalkBack enabled for accessibility features

---

## Emergency Hotfix Process

Even for urgent fixes, maintain review discipline:

1. **Create hotfix branch**: `git checkout -b hotfix/critical-bug`
2. **Implement minimal fix** (no feature creep)
3. **Quick LLM review**: Use DEV agent `*code-review`
4. **Test thoroughly** (hotfixes often introduce new bugs)
5. **Commit with detailed message**
6. **Merge and deploy**
7. **Schedule proper fix** if hotfix is temporary

---

## Commit Message Standards

After successful code review, use descriptive commit messages:

### Format
```
<Type>: <Short summary (≤50 chars)>

<Detailed description>
- What changed
- Why it changed  
- Impact/side effects

<Story/Issue reference>
<Testing notes>
```

### Examples
```
Story 3.1 Complete: Settings screen with Material Design 3

IMPLEMENTED FEATURES:
- SettingsFragment with Material 3 theme
- Preference categories: Recognition, Accessibility, About
- DataStore integration for persistence

ACCEPTANCE CRITERIA STATUS:
✅ AC1: Settings accessible from home screen
✅ AC2: All preferences save correctly
✅ AC3: TalkBack navigation functional

FILES MODIFIED:
- A app/src/main/java/com/visionfocus/ui/settings/SettingsFragment.kt
- M app/src/main/res/navigation/nav_graph.xml

TESTING:
- Unit tests passing: SettingsViewModelTest
- Manual TalkBack testing completed
```

---

## Integration with Existing Workflow

This guideline complements `DO-NOT-COMMIT.md`:
- **DO-NOT-COMMIT.md**: High-level BMAD workflow and phase progression
- **BMAD-DEVELOPMENT-GUIDELINES.md**: Detailed development and commit standards

Both documents work together to ensure quality delivery.

---

## Enforcement

### For Solo Developers
- Self-discipline is key
- Use LLM code review as minimum standard
- Keep a review log (optional but helpful)

### For Academic Projects (Like VisionFocus)
- Code review demonstrates professional practice
- Documents quality assurance for dissertation
- Shows understanding of software engineering principles

### For Team Projects
- Pull request reviews mandatory
- No direct commits to main/master
- CI/CD gates enforce testing requirements

---

## Questions or Issues?

If you're unsure whether code is ready to commit:
- **Run code review** → Address findings → Commit
- When in doubt, **always review first**

Remember: **Time spent on code review is time saved on debugging and refactoring.**

---

**Last Updated**: December 30, 2025  
**Project**: VisionFocus - Accessible Object Recognition for Visually Impaired Users  
**Author**: Allan (MSC Final Project)
