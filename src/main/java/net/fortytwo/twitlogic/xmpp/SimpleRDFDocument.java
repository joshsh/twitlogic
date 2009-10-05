package net.fortytwo.twitlogic.xmpp;

import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: Sep 24, 2009
 * Time: 10:33:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleRDFDocument implements RDFDocument {
    private final Collection<Statement> statements;

    public SimpleRDFDocument(final Collection<Statement> statements) {
        this.statements = statements;
    }

    public SimpleRDFDocument(final Repository repo) throws RepositoryException, RDFHandlerException {
        statements = new LinkedList<Statement>();

        RDFHandler handler = new RDFHandler() {
            public void startRDF() throws RDFHandlerException {
            }

            public void endRDF() throws RDFHandlerException {
            }

            public void handleNamespace(String s, String s1) throws RDFHandlerException {
            }

            public void handleStatement(final Statement statement) throws RDFHandlerException {
                statements.add(statement);
            }

            public void handleComment(String s) throws RDFHandlerException {
            }
        };

        RepositoryConnection rc = repo.getConnection();
        try {
            rc.export(handler);
        } finally {
            rc.close();
        }
    }

    public void writeTo(final RDFHandler handler) {
        try {
            handler.startRDF();
            try {
                for (Statement st : statements) {
                    handler.handleStatement(st);
                }
            } finally {
                handler.endRDF();
            }
        } catch (RDFHandlerException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
