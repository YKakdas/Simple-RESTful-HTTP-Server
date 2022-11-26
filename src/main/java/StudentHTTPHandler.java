import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

public class StudentHTTPHandler implements HttpHandler {

    // Parse incoming request, and take an action regarding the information gained from the request
    @Override
    public void handle(HttpExchange httpExchange) {
        switch (httpExchange.getRequestMethod()) {
            // If the method is GET, try to parse the uri to understand the request
            case "GET" -> {
                String param = parseUserIDFromRequest(httpExchange);
                // If the request does not follow the server side constraints, send response as 400 Bad Request
                if (param == null) {
                    sendBadRequest(httpExchange.getRequestURI().toString(), httpExchange);
                } else if (param.isEmpty()) {
                    // If no id specified, return all students
                    returnAllStudents(httpExchange);
                } else {
                    // ID is specified, return a specific student with that ID
                    returnStudentWithID(param, httpExchange);
                }
            }
            // If the request is POST, create new student record
            case "POST" -> createNewStudent(httpExchange);
            // If the request is PUT, try to parse the student id from the request uri.
            case "PUT" -> {
                String param = parseUserIDFromRequest(httpExchange);
                // If the request does not follow the constraints, send 400 Bad Request
                if (param == null) {
                    sendBadRequest(httpExchange.getRequestURI().toString(), httpExchange);
                } else if (!param.isEmpty()) {
                    // If ID is found in the request, update the record of given student
                    updateStudentWithID(param, httpExchange);
                }
            }
            // If the request is DELETE, try to parse the student id from the request uri.
            case "DELETE" -> {
                String param = parseUserIDFromRequest(httpExchange);
                // If the request does not follow the constraints, send 400 Bad Request
                if (param == null) {
                    sendBadRequest(httpExchange.getRequestURI().toString(), httpExchange);
                } else if (!param.isEmpty()) {
                    // If ID is found in the request, delete the record of given student
                    deleteStudentWithID(param, httpExchange);
                }
            }
        }

    }

    // Excepted pattern should either be 127.0.0.1/students or 127.0.0.1/students/id
    // Try to parse the request uri, return the id if possible, else return null for indicating an error
    private String parseUserIDFromRequest(HttpExchange httpExchange) {
        String requestUri = httpExchange.getRequestURI().toString();
        String[] split = requestUri.split("/");
        if (split.length < 3) {
            return "";
        } else if (split.length == 3) {
            return split[2];
        } else {
            return null;
        }
    }

    // Send 200 OK with all students in the response body as list
    private void returnAllStudents(HttpExchange httpExchange) {
        String students = new Gson().toJson(StudentHTTPServer.students);
        sendSuccessWithMessage(students, httpExchange);
    }

    // Try to get a student with a specific ID. If there is none, send 400 Bad Request, if it is found,
    // send 200 OK with that student in response body.
    private void returnStudentWithID(String studentId, HttpExchange httpExchange) {
        int id = Integer.parseInt(studentId);
        Optional<Student> optional = getStudentWithId(id);
        if (optional.isPresent()) {
            String studentJson = new Gson().toJson(optional.get());
            sendSuccessWithMessage(studentJson, httpExchange);
        } else {
            String message = "Server could not find a student with id of " + studentId;
            sendBadRequestWithMessage(message, httpExchange);
        }
    }

    // Filter student list to find a specific student with given id
    private Optional<Student> getStudentWithId(int id) {
        return StudentHTTPServer.students.stream().filter(student -> student.getStudentId() == id).findFirst();
    }

    // Create new student record by converting request body Json entry to Student.class
    private void createNewStudent(HttpExchange httpExchange) {
        try {
            InputStream inputStream = httpExchange.getRequestBody();
            byte[] studentBytes = inputStream.readAllBytes();
            String json = new String(studentBytes, 0, studentBytes.length);
            Student student = new Gson().fromJson(json, Student.class);
            Optional<Student> optional = getStudentWithId(student.getStudentId());

            // If the student with an id already exists, send 400 Bad Request.
            if (optional.isPresent()) {
                sendBadRequestWithMessage("The student with given id already exists in the server!", httpExchange);
            } else {
                // If everything ok, add the student into the list, and send 200 OK.
                StudentHTTPServer.students.add(student);
                sendSuccessWithMessage("New student successfully created in server!", httpExchange);
            }
        } catch (Exception e) {
            // When unexpected error occurs, send 500 Internal Server Error.
            sendInternalServerErrorWithMessage("Server encountered with an " +
                    "unexpected error while creating a new student record!", httpExchange);
            e.printStackTrace();
        }

    }

    // Update student record for given student id
    private void updateStudentWithID(String studentId, HttpExchange httpExchange) {
        int id = Integer.parseInt(studentId);
        Optional<Student> optional = getStudentWithId(id);
        // If student record has been found for given id, try to read Student.class from request body json,
        // if successful, update the student record, send 200 OK.
        if (optional.isPresent()) {
            try {
                InputStream inputStream = httpExchange.getRequestBody();
                byte[] studentBytes = inputStream.readAllBytes();
                String json = new String(studentBytes, 0, studentBytes.length);
                Student newStudent = new Gson().fromJson(json, Student.class);
                Student oldStudent = optional.get();
                StudentHTTPServer.students.set(StudentHTTPServer.students.indexOf(oldStudent), newStudent);
                String message = "Record of the given student updated!";
                sendSuccessWithMessage(message, httpExchange);
            } catch (Exception e) {
                // When unexpected error occurs, send 500 Internal Server Error.
                sendInternalServerErrorWithMessage("Server encountered with an " +
                        "unexpected error while updating the student record!", httpExchange);
                e.printStackTrace();
            }

        } else {
            String message = "Server could not find a student with id of " + studentId;
            sendBadRequestWithMessage(message, httpExchange);
        }
    }

    // Delete student record for given student id
    private void deleteStudentWithID(String studentId, HttpExchange httpExchange) {
        int id = Integer.parseInt(studentId);
        Optional<Student> optional = getStudentWithId(id);
        // If student with given id has been found, delete its record and send 200 OK
        if (optional.isPresent()) {
            StudentHTTPServer.students.remove(optional.get());
            sendSuccessWithMessage("The student has been deleted from the records!", httpExchange);
        } else {
            // If student with given ID does not exist, send 400 Bad Request
            String message = "Server could not find a student with id of " + studentId;
            sendBadRequestWithMessage(message, httpExchange);
        }
    }

    // Generic function that sends 200 OK with a message in its body.
    private void sendSuccessWithMessage(String message, HttpExchange httpExchange) {
        try {
            httpExchange.getResponseHeaders().set("Content-Type:", "application/json");
            httpExchange.sendResponseHeaders(200, message.length());
            OutputStream out = httpExchange.getResponseBody();
            out.write(message.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Generic function that sends 500 Internal Server Error with a message in its body.
    private void sendInternalServerErrorWithMessage(String message, HttpExchange httpExchange) {
        try {
            httpExchange.sendResponseHeaders(500, message.length());
            OutputStream out = httpExchange.getResponseBody();
            out.write(message.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Generic function that sends 400 Bad Request when request uri could not be parsed as desired.
    private void sendBadRequest(String uri, HttpExchange httpExchange) {
        try {
            String message = "Invalid query with uri of " + uri + ". Server is unable to process the request!";
            httpExchange.sendResponseHeaders(400, message.length());
            OutputStream out = httpExchange.getResponseBody();
            out.write(message.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Generic function that sends 400 Bad Request with a message in its body.
    private void sendBadRequestWithMessage(String message, HttpExchange httpExchange) {
        try {
            httpExchange.sendResponseHeaders(400, message.length());
            OutputStream out = httpExchange.getResponseBody();
            out.write(message.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
