Jagr Modules

core




launcher
- Single assignment grading

App
- With DB
-


application
- usecases
  - new-assignment
    - usecase.kt
    - controller.kt
  - start-assignment
  - change-assignment
  - new-user
- trigger
  - http trigger
  - git trigger

application-domain < launcher-domain
- users
- assignments

launcher

launcher-domain
- grader
- ratings
- submissions


infrastructure
-
