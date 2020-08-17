# Silene Middleware Library v0.1.1

Silene or more formally `com.vulpex.silene` is a  Java Library that can be used to
interract with ProjectGeorge backend in pure Java with OOP.

Just get the `com.vulpex.silene.jar` file to your libraries directory, and 
`import com.vulpex.silene.*` to start using the classes. You will also need the 
[Google JSON Library (gson)](https://github.com/google/gson)

> :warning: You can read extensive Javadoc documention in docs/index.html after cloning
> the repository.

## Users

### How to Register A User?

Registering a user is handled by that `User` subclasses register static method.

#### Registering a Student

```java
Student.registerStudent("username", "name", "password", "locality");
```

#### Registering a Tutor

```java
Tutor.registerTutor("username", "name", "password", "locality", "expertise", "allowedWeekdays");
```

Keep in mind, for `Tutor`s, `allowedWeekdays` is a string that keeps the index of
the allowed weekdays, indexed from Sunday. So, `034` means this `Tutor` is available
for Sunday, Wednesday, Thursday.

### How to Sign In an Existing Student?

At this point, only available login method is a native login, in the future,
we hope to add Facebook and Google sign ins. To sign in an existent user, you
need to use the `User.loginUser(String username, String password)` static method.

This method will return a `User` subclass, the developer must check the type of
this class to decide whether it is a `Student` or a `Tutor`

```java
User user = User.loginUser("thegraphguy", "shortest");
if (user instanceof Student) {
    Student student = (Student) user;
} else if (user instanceof Tutor) {
    Tutor tutor = (Tutor) user;
}
```

### Searching for Tutors

Once a user is signed in, that user can be used to search for Tutors.

```java
Student student = (Student) User.loginUser("thegraphguy", "shortest");
List<Tutor> tutors = student.searchForTutors("Mathematics", "London");
```

This will give you a `List` of `Tutor` objects.