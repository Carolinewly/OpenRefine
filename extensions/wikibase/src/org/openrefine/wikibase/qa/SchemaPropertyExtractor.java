
package org.openrefine.wikibase.qa;

import java.util.*;

import org.openrefine.wikibase.schema.*;
import org.openrefine.wikibase.updates.EntityEdit;
import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;

public class SchemaPropertyExtractor {

    public Set<PropertyIdValue> getAllProperties(WikibaseSchema schema) {
        Set<PropertyIdValue> properties = new HashSet<>();
        List<WbExpression<? extends EntityEdit>> entityDocumentExprs = schema.getEntityDocumentExpressions();
        for (WbExpression<? extends EntityEdit> entityDocumentExpr : entityDocumentExprs) {
            List<WbStatementGroupExpr> statementGroups = Collections.emptyList();
            if (entityDocumentExpr instanceof WbItemEditExpr) {
                statementGroups = ((WbItemEditExpr) entityDocumentExpr).getStatementGroups();
            } else if (entityDocumentExpr instanceof WbMediaInfoEditExpr) {
                statementGroups = ((WbMediaInfoEditExpr) entityDocumentExpr).getStatementGroups();
            } else {
                throw new IllegalStateException("Unsupported entity type");
            }
            for (WbStatementGroupExpr statementGroup : statementGroups) {
                WbExpression<? extends PropertyIdValue> statementGroupProperty = statementGroup.getProperty();
                if (statementGroupProperty instanceof WbPropConstant) {
                    properties.add(Datamodel.makeWikidataPropertyIdValue(((WbPropConstant) statementGroupProperty).getPid()));
                }
                List<WbStatementExpr> statementExprs = statementGroup.getStatements();
                for (WbStatementExpr statementExpr : statementExprs) {
                    List<WbSnakExpr> snakExprs = new ArrayList<>(statementExpr.getQualifiers());
                    List<WbReferenceExpr> referenceExprs = statementExpr.getReferences();
                    for (WbReferenceExpr referenceExpr : referenceExprs) {
                        snakExprs.addAll(referenceExpr.getSnaks());
                    }

                    for (WbSnakExpr snakExpr : snakExprs) {
                        WbExpression<? extends PropertyIdValue> qualifierProperty = snakExpr.getProp();
                        if (qualifierProperty instanceof WbPropConstant) {
                            properties.add(Datamodel.makeWikidataPropertyIdValue(((WbPropConstant) qualifierProperty).getPid()));
                        }
                    }
                }
            }
        }

        return properties;
    }
}
