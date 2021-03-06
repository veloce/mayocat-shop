package org.mayocat.shop.front.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import org.mayocat.configuration.general.GeneralSettings;
import org.mayocat.context.WebContext;
import org.mayocat.shop.front.FrontContextManager;
import org.mayocat.shop.front.FrontContextSupplier;
import org.mayocat.shop.front.annotation.FrontContextContributor;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @version $Id$
 */
@Component
public class DefaultFrontContextManager implements FrontContextManager, Initializable
{
    @Inject
    private Map<String, FrontContextSupplier> contextSuppliers;

    @Inject
    private Logger logger;

    @Inject
    private WebContext context;

    private Node<Binding> bindings;

    class Binding
    {
        Binding(String path)
        {
            this.path = path;
        }

        private String path;

        private Map<Method, Object> methods = Maps.newHashMap();
    }

    class Node<T>
    {
        private T data;

        private List<Node<T>> children;
    }

    @Override
    public Map<String, Object> getContext(final UriInfo uriInfo)
    {
        Map result = Maps.newHashMap();

        // Manage list of locales and corresponding links
        Map<String, Map<String, String>> locales = Maps.newHashMap();
        GeneralSettings settings = context.getSettings(GeneralSettings.class);
        final Locale mainLocale = settings.getLocales().getMainLocale().getValue();
        locales.put(mainLocale.toLanguageTag(), buildLocale(mainLocale, uriInfo, true));
        List<Locale> alternativeLocales = Objects.firstNonNull(
                settings.getLocales().getOtherLocales().getValue(), ImmutableList.<Locale>of());
        if (!alternativeLocales.isEmpty()) {
            for (final Locale locale : alternativeLocales) {
                locales.put(locale.toLanguageTag(), buildLocale(locale, uriInfo, false));
            }
        }

        result.put("locales", locales);
        result.put("locale", buildLocale(context.getLocale(), uriInfo, mainLocale.equals(context.getLocale())));
        result.put("localePath", context.isAlternativeLocale() ? ("/" + context.getLocale().toLanguageTag()) : "");
        result.put("url", result.get("localePath") + "/" + uriInfo.getPath());
        result.put("canonicalUrl", "/" + uriInfo.getPath());

        // Root path ('/')
        invokeNodeMethods(result, bindings);

        // Children path
        if (!isRoot(uriInfo.getPathSegments())) {
            walkNode(bindings, uriInfo.getPathSegments(), result);
        }
        return result;
    }

    private Map<String, String> buildLocale(final Locale locale, final UriInfo uriInfo, final boolean isMainLocale)
    {
        return new HashMap()
        {
            {
                put("url", (isMainLocale ? "" : "/" + locale.toLanguageTag()) + "/" + uriInfo.getPath());
                put("tag", locale.toLanguageTag());
                put("code", locale.getLanguage());
                put("country", locale.getDisplayCountry(locale));
                put("language", locale.getDisplayLanguage(locale));
                put("current", locale.equals(context.getLocale()));
            }
        };
    }

    @Override
    public void initialize() throws InitializationException
    {
        for (FrontContextSupplier supplier : contextSuppliers.values()) {
            for (Method method : supplier.getClass().getMethods()) {
                if (method.isAnnotationPresent(FrontContextContributor.class)) {
                    addMethod(method, supplier);
                }
            }
        }
    }

    private void walkNode(Node<Binding> binding, List<PathSegment> pathSegments, Map data)
    {
        List<PathSegment> segments = new ArrayList<PathSegment>(pathSegments);

        PathSegment currentSegment = pathSegments.get(0);

        for (Node<Binding> child : binding.children) {
            if (child.data.path.equals(currentSegment.getPath()) || child.data.path.equals("*")) {
                invokeNodeMethods(data, child);
                if (segments.size() > 1) {
                    walkNode(child, segments.subList(1, segments.size()), data);
                }
            }
        }
    }

    private void invokeNodeMethods(Map data, Node<Binding> node)
    {
        for (Method m : node.data.methods.keySet()) {
            try {
                Object target = node.data.methods.get(m);
                m.invoke(target, data);
            } catch (IllegalAccessException e) {
                this.logger.error("Failed to get bindings", e);
            } catch (InvocationTargetException e) {
                this.logger.error("Failed to get bindings", e);
            }
        }
    }

    private boolean isRoot(List<PathSegment> segments)
    {
        if (segments.size() == 1 && segments.get(0).getPath().equals("")) {
            return true;
        }
        return false;
    }

    private void addMethod(Method method, Object target)
    {
        if (bindings == null) {
            bindings = new Node<Binding>();
            bindings.data = new Binding("");
            bindings.children = Lists.newArrayList();
        }

        FrontContextContributor annotation = method.getAnnotation(FrontContextContributor.class);
        String path = annotation.path();
        Node<Binding> methodBindings = bindings;
        String[] pathSegments = path.split("/");

        if (pathSegments.length == 0) {
            addMethodToNode(methodBindings, method, target);
        } else {
            boolean isRoot = true;
            while (pathSegments.length > 0) {
                String segment = pathSegments[0];
                boolean leafOrChildrenFound = false;
                for (Node<Binding> node : methodBindings.children) {
                    if (node.data.path.equals(segment) && pathSegments.length == 1) {
                        addMethodToNode(node, method, target);
                        leafOrChildrenFound = true;
                    } else if (node.data.path.equals(segment)) {
                        methodBindings = node;
                        leafOrChildrenFound = true;
                    }
                }
                if (!leafOrChildrenFound && !isRoot) {
                    Binding newChild = new Binding(segment);
                    Node<Binding> childNode = new Node<Binding>();
                    childNode.data = newChild;
                    childNode.children = Lists.newArrayList();
                    methodBindings.children.add(childNode);
                    methodBindings = childNode;
                    if (pathSegments.length == 1) {
                        addMethodToNode(childNode, method, target);
                    }
                }
                isRoot = false;
                pathSegments = Arrays.copyOfRange(pathSegments, 1, pathSegments.length);
            }
        }
    }

    private void addMethodToNode(Node<Binding> node, Method method, Object target)
    {
        node.data.methods.put(method, target);
    }
}
