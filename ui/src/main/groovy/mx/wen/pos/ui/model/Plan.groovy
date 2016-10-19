package mx.wen.pos.ui.model

import groovy.beans.Bindable
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@Bindable
@ToString
@EqualsAndHashCode
class Plan {
  String id
  String description

  static Plan toPlan( mx.wen.pos.model.Plan plan ) {
    if ( plan?.id ) {
      Plan tmpPlan = new Plan(
          id: plan.id.trim(),
          description: plan.descripcion
      )
      return tmpPlan
    }
    return null
  }
}
