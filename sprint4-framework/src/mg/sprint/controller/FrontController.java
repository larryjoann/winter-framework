package mg.sprint.controller;

import jakarta.servlet.ServletException; // Assurez-vous que cette importation est présente
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.sprint.annotation.Controller;
import mg.sprint.annotation.GetMapping;
import mg.sprint.reflection.Reflect;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class FrontController extends HttpServlet {
    private final HashMap<String, Mapping> urlMappings = new HashMap<>();

    @Override
    public void init() {
        // Récupérer le package des contrôleurs à partir des paramètres d'initialisation
        String controllersPackage = this.getInitParameter("controllers_package");
        try {
            // Scanner les fichiers pour trouver les classes annotées @Controller
            List<Class<?>> controllers = Reflect.getAnnotatedClasses(controllersPackage, Controller.class);
            for (Class<?> controller : controllers) {
                for (Method method : controller.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(GetMapping.class)) {
                        GetMapping getMapping = method.getAnnotation(GetMapping.class);
                        String url = getMapping.value();
                        // Vérifier que l'URL n'est pas déjà mappée
                        if (urlMappings.containsKey(url)) {
                            throw new IllegalStateException("L'URL " + url + " est déjà mappée.");
                        }
                        // Ajouter l'URL et la méthode à la liste des mappings
                        urlMappings.put(url, new Mapping(controller, method));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        processRequest(req, resp);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String url = requestURI.substring(contextPath.length());
        Mapping mapping = urlMappings.get(url);

        if (mapping == null) {
            // Si l'URL n'est pas mappée, renvoyer une erreur 404
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("<h1>Erreur 404</h1>");
            out.println("<p>L'URL " + url + " est introuvable sur ce serveur, veuillez essayer un autre.</p>");
        } else {
            try {
                // Instancier le contrôleur
                Object controllerInstance = mapping.getController().getDeclaredConstructor().newInstance();

                // Appeler la méthode du contrôleur
                Method method = mapping.getMethod();
                Object result = method.invoke(controllerInstance);

                if (result instanceof String) {
                    // Si le résultat est une chaîne de caractères, renvoyer le texte brut
                    resp.setContentType("text/plain");
                    out.println((String) result);
                } else if (result instanceof ModelView) {
                    // Si le résultat est un ModelView, traiter la vue associée
                    ModelView mv = (ModelView) result;
                    String viewUrl = mv.getUrl();
                    HashMap<String, Object> data = mv.getData();
                    // Transférer les données vers la vue
                    for (String key : data.keySet()) {
                        req.setAttribute(key, data.get(key));
                    }
                    try {
                        // Faire une redirection vers la vue associée
                        req.getRequestDispatcher(viewUrl).forward(req, resp);
                    } catch (ServletException e) {
                        // En cas d'erreur lors de la redirection, renvoyer une erreur 500
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        out.println("<h1>500 Internal Server Error</h1>");
                        out.println(
                                "<p>Une erreur s'est produite lors de l'appel à la vue : " + e.getMessage() + "</p>");
                        e.printStackTrace(out);
                    }
                } else {
                    // Si le type de retour n'est pas reconnu, renvoyer une erreur
                    resp.setContentType("text/plain");
                    out.println("Type de retour non reconnu.");
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException e) {
                // En cas d'erreur lors de l'invocation du contrôleur, renvoyer une erreur 500
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("<h1>500 Internal Server Error</h1>");
                out.println(
                        "<p>Une erreur s'est produite lors de l'invocation du contrôleur : " + e.getMessage() + "</p>");
                e.printStackTrace(out);
            }
        }
    }
}
