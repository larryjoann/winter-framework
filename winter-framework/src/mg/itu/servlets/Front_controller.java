package mg.itu.servlets;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.util.*;
import java.net.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import mg.itu.annotation.*;
import mg.itu.beans.Mapping;

public class Front_controller  extends HttpServlet {

    protected String package_name;
    protected HashMap <String, Mapping> lists;

    @Override
    public void init(ServletConfig config) throws ServletException {
        //  Auto-generated method stub
        /**
         * maka an le param-value (web.xml) -> package
         */
        super.init(config);
        this.package_name = config.getInitParameter("package_controllers");
        lists = new HashMap <String, Mapping> ();
        this.check_controller(package_name);
    }


    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");


        // CHECK CONTROLLER _______

        try (PrintWriter out = response.getWriter()) {
            out.println(request.getRequestURL() +"</br>");
            
            String mapp = this.get_dernier_uri(request.getRequestURI());

            Mapping mapping = lists.get(mapp);
            if (mapping != null) {
                out.println("<b> uri:["+mapp+"] "+mapping.toString()+"</b>");
            } else {
                out.println("<b>tsy misy</b>");
            }
            
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    private String get_dernier_uri (String request_uri) {
        int indexlast_slash = request_uri.lastIndexOf('/');
        if (indexlast_slash == -1) {
            // SI AUCUN N'EST TROUVER ______
            return request_uri;
        } else {
            return request_uri.substring(indexlast_slash+1);
        } 
    }


    private void check_controller (String pack) {
        /**
         * maka ny controller reetr
         * ka Annoter controller 
         */

        // ____ charger pack ____
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String package_path = pack.replace('.', '/');
        URL resource = classLoader.getResource(package_path);
        try {
            Path package_ = Paths.get(resource.toURI());
            Files.walk(package_).filter(fichier -> fichier.toString().endsWith(".class"))
            // ____ parcourir les fichiers ____
            .forEach(fichier -> {
                String path_class = pack+"."+fichier.getFileName().toString().replace(".class", "");
                try {
                    Class <?> classs = Class.forName(path_class);
                    this.valid_controller(classs);
                } catch (ClassNotFoundException e) {
                    // Auto-generated catch block
                    e.printStackTrace();
                }
            });

        } catch (URISyntaxException | IOException e) {
            // Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void valid_controller (Class <?> classe) {

        if (classe.isAnnotationPresent(Annotation_controller.class)
        && !Modifier.isAbstract(classe.getModifiers())) {
            Method[] methods = classe.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Get.class)) {
                    Get get = method.getAnnotation(Get.class);
                    this.lists.put(get.url(), new Mapping(classe.getSimpleName(), method.getName()));
                } // else SKIP _______
            }
        }
        // else skip _______
    }

    /*** doGet */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** * doPost */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

}
