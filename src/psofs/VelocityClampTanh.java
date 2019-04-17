/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package psofs;

/**
 *
 * @author xuebing
 */
public class VelocityClampTanh extends VelocityClamp {

    public VelocityClampTanh() {
    }

    public void clamp(Particle p, double MaxVelocity, double MinVelocity) {


        for (int i = 0; i < p.getSize(); ++i) {
            //This should be the absolute value... TODO
            p.setVelocity(i, MaxVelocity*Math.tanh(p.getVelocity(i) / MaxVelocity));

//
//            if (p.getVelocity(i) > MaxVelocity) {
//                p.setVelocity(i, MaxVelocity);
//            } else if (p.getVelocity(i) < MinVelocity) {
//                p.setVelocity(i, MinVelocity);
//            }
        }
    }
}

//   void TanhClamping::clamp(Particle* p) {
//      for (int i = 0; i < p->size(); ++i) {
//         p->setVelocity(i, maxVelocity(i) *
//                 tanh(p->velocity(i) / p->maxVelocity(i)));
//      }
//   }
//
//   std::string TanhClamping::toString() const{
//      return "Tanh";
//   }
//}