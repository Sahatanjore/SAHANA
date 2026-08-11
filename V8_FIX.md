AINA v8 build fix

The uploaded GitHub Actions log still contains both:
- app/src/main/res/drawable/ic_sahana_logo.png
- app/src/main/res/drawable/ic_sahana_logo.xml

That means the repository still had the stale XML resource when the build ran.
v8 adds a workflow cleanup step that deletes any stale ic_sahana_logo.xml/svg
before Gradle runs, while keeping the real PNG logo.
