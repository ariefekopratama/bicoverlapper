package biclustering;

import java.util.*;

/**
 * Generic Class for Biclustering
 * TODO: STILL IN DEVELOPMENT
 * @author Rodrigo Santamaria
 *
 */
public class Biclustering {

  static int GENE_SIZE_CONSTRAINT = 0;
  static int CHIP_SIZE_CONSTRAINT = 0;

  static int CHIP_BLOCK_SIZE = 10;  // arbitrary: the smallest chip size, when no switching lohnt sich mehr

  static int geneCount;
  static int chipCount;

  static BitSet[] geneStars;
  static BitSet[] chipStars;

  // ===========================================================================
  public Biclustering(BitSet[] gs, BitSet[] cs) {
    geneCount = gs.length;
    chipCount = cs.length;

    geneStars = new BitSet[geneCount];
    for (int i = 0; i < geneCount; i++) geneStars[i] = gs[i];

    chipStars = new BitSet[chipCount];
    for (int i = 0; i < chipCount; i++) chipStars[i] = cs[i];

      // parameter nulling?
    gs = null;
    cs = null;
  }

  // ===========================================================================
  public void setBiclusterSizeConstraints(int gd, int cd) {
    GENE_SIZE_CONSTRAINT = gd;
    CHIP_SIZE_CONSTRAINT = cd;
  }

  // ===========================================================================
  // 07.08.2004 -> seem to work: 09.08.04
  public HashMap computeBiclusters(BitSet g_mask, BitSet c_mask,
                                   BitSet cr_mask, BitSet cl_mask,
                                   String msg, int base,
                                   boolean print_flag) {

    //System.out.println(base);
    HashMap bcs = new HashMap();

 //  System.out.print(msg+": [P_CR]="+cr_mask.toString()+", [P_CL]="+cl_mask.toString());
    if(g_mask.cardinality() == 0 || c_mask.cardinality() == 0) {
      System.out.println("(g,c) = (0,0) ?!?"); //System.exit(99);
      return bcs; // das sollte nie auftreten.
    }

    if(   g_mask.cardinality() < GENE_SIZE_CONSTRAINT
       || c_mask.cardinality() < CHIP_SIZE_CONSTRAINT) return bcs;   // size constraints

    BitSet[] blocks;
    // if(geneCount > chipCount) {

      // this works fine: 2884 x 17
    if(g_mask.cardinality() <= c_mask.cardinality())
      blocks = blockTheMatrix(g_mask,c_mask, false); //true);
    else
      blocks = blockTheMatrix_Switch(g_mask, c_mask, false); //true);

/*    System.out.println("hdr = "+blocks[0].toString());
    System.out.println(" bc = "+blocks[1].toString());
    System.out.println(" cr = "+blocks[2].toString());
    System.out.println(" cl = "+ blocks[3].toString());

    System.exit(100);
*/
    BitSet header = blocks[0];
    BitSet b_BC   = blocks[1];
    BitSet cr     = blocks[2];
    BitSet cl     = blocks[3];

    // -------------------------------------------------------------------------
    // all made of zeros (SHOULD NEVER HAPPEN)...
    if(   cr.cardinality() == 0
       && b_BC.cardinality() == 0) {

//  System.out.println("case 1");

      // debug
      System.out.println("(cr,b_BC) = (0,0) ?!?");
      //System.exit(1);
      return bcs; //??
    }

    if(   header.cardinality() == g_mask.cardinality()
       && cl.cardinality() == c_mask.cardinality()) {

 //  System.out.println("case 2");
     // debug
      System.out.println("all zeros. Can this be ???");
      //System.exit(11);
      return bcs;
    }

    // -------------------------------------------------------------------------
    // (+) no zero eck:
    if(header.cardinality() == 0) {   // weil je to post-condition koja to osigura
    //  System.out.println("case 3");

      // debug
      if(cl.cardinality() > 0) {
        System.out.println("Impossible: (hdr, cl) = (0, >0) !!!");
        //System.exit(2);
      }

      bcs.put(g_mask, c_mask);

   //   System.out.println("\nEND CASE: "+g_mask.toString()+", "+c_mask.toString());
      if(print_flag) {
        System.out.println("\nEND CASE: "+g_mask.toString()+", "+c_mask.toString());
        print_submatrix_content(g_mask, c_mask);
        System.out.println();
      }
      return bcs;
    }

    // -------------------------------------------------------------------------
    // (+) horizontal zero pojas:
    if(   header.cardinality() > 0
       && cl.cardinality() == c_mask.cardinality()) {
 //  System.out.println("case 4");

      // debug
      if(cr.cardinality() < c_mask.cardinality()) {
        System.out.println("Impossible: (hdr, cl;cr) = (>0, ==c_mask, !=c_mask) !!!");
        //System.exit(3);
      }

      if(print_flag) {
        System.out.println("Horizontal zero pojas:");
        print_submatrix_content(g_mask, c_mask);
      }

      return computeBiclusters(b_BC, c_mask, cr_mask, cl_mask, msg, (base+1), false);
    }

    // -------------------------------------------------------------------------
    // (+) vertical zero pojas:
    if(header.cardinality() == g_mask.cardinality()) { // && cr.cardinality() > 0) {

     // System.out.println("case 5");
     // debug
      if(cr.cardinality() == 0) {
        System.out.println("Impossible: (hdr, cr) = (g_mask, 0) ???");
        //System.exit(44);
      }
      // debug
      if(b_BC.cardinality() < g_mask.cardinality()) {
        System.out.println("Impossible: (hdr, b_BC) = (==g_mask, !=g_mask) !!!");
        //System.exit(4);
      }

      if(print_flag) {
        System.out.println("Vertical zero pojas:");
        print_submatrix_content(g_mask, c_mask);
      }

      if(   msg.equals("top") == false
         // && cr.cardinality() > 0
         && (isSubset(cr, cr_mask) || isSubset(cr, cl_mask)))
        return bcs;

      return computeBiclusters(g_mask, cr,
                               getIntersection(cr, cr_mask),  // strip-off the vertical zero pojas!
                               getIntersection(cr, cl_mask),  // ...
                               msg, (base+1), false);
    }

    // -------------------------------------------------------------------------
    // "normal" zero eck:
    if(header.cardinality() > 0) {

    //  System.out.println("case 6");
   // debug:
      if(cl.cardinality() == 0) {
        System.out.println("Impossible: (hdr, cl) = (> 0, 0) !!!");
        //System.exit(5);
      }
      if(cr.cardinality() == 0) {
        System.out.println("Impossible: (hdr, cr) = (> 0, 0) !!!");
        //System.exit(55);
      }

      if (print_flag) {
        System.out.println("General 1-eckig case:");
        print_submatrix_content(g_mask, c_mask);
      }

      // -----------------------------------------------------------------
      if (msg.equals("top")) {  // solve all!!!!
      //  System.out.println("case 7");

        HashMap alpha = computeBiclusters(g_mask, cr,
                                          getIntersection(cr, cr_mask),
                                          getIntersection(cr, cl_mask),
                                          msg, (base + 1), false); // ok.?

        HashMap gamma = computeBiclusters(b_BC, cl,
                                          getIntersection(cl, cr_mask),
                                          getIntersection(cl, cl_mask),
                                          msg, (base+1), false);

        HashMap beta = computeBiclusters(b_BC, c_mask,
                                         cr, cl,
                                         "b", (base+1), false);

        return mergeHashes(mergeHashes(alpha, gamma), beta);
      }

      // -----------------------------------------------------------------
      else {
      // System.out.println("case 8");

        HashMap alpha = new HashMap();
        HashMap beta  = new HashMap();

        // -*-*-*-*-
        if((isSubset(cr, cr_mask) || isSubset(cr, cl_mask))
           &&
           (isSubset(cl, cr_mask) || isSubset(cl, cl_mask))) {
       //  System.out.println("case 9");

          beta = computeBiclusters(b_BC, c_mask,
                                   cr_mask, cl_mask,
                                   "b", (base+1), false);
        }
        // -*-*-*-*-
        else
        if(isSubset(cr, cr_mask) || isSubset(cr, cl_mask)) { // "regular" case
      //    System.out.println("case 10");

          beta = computeBiclusters(b_BC, c_mask,
                                   cr_mask, cl_mask,
                                   "b",(base+1), false);
        }

        // -*-*-*-*-
        else
        if(isSubset(cl, cr_mask) || isSubset(cl, cl_mask)) { // another "regular" case
        // System.out.println("case 11");

          alpha = computeBiclusters(g_mask, cr,
                                    getIntersection(cr, cr_mask),
                                    getIntersection(cr, cl_mask),
                                    "a", (base+1), false);

          beta = computeBiclusters(b_BC, c_mask,
                                   cr_mask, cl_mask,
                                   "b",(base+1), false);

          beta = cleanMasked(beta, cr);
        }

        // -*-*-*-*-
        else {
        //System.out.println("case 12");

          // the irregular case:

          alpha = computeBiclusters(g_mask, cr,
                                    getIntersection(cr, cr_mask),
                                    getIntersection(cr, cl_mask),
                                    "a", (base+1), false);

          HashMap gamma = computeBiclusters(b_BC, cl,
                                            getIntersection(cl, cr_mask),
                                            getIntersection(cl, cl_mask),
                                            "a", (base+1), false);

          alpha = mergeHashes(alpha, gamma);

          beta = computeBiclusters(b_BC, c_mask,
                                   blocks[2], blocks[3],
                                   "b",(base+1), false);
          beta = cleanMasked(beta, cr_mask);
          beta = cleanMasked(beta, cl_mask);
        }

        return mergeHashes(alpha,beta);
      }

      // -----------------------------------------------------------------

    }

    System.out.println("arrived here? SHOULD NEVER HAPPEN!");
    //System.exit(2);
    return bcs;
  }


  // ===========================================================================
  public HashMap computeBiclusters_NewDataStructure(int[][] submatrix,
      BitSet cr_mask, BitSet cl_mask,
      String msg, int base,
      boolean print_flag) {

    // new:

    //System.out.println(msg+":"+", "+base);
    if(submatrix.length == 0) return new HashMap();
    int g_mask_card = submatrix.length;
    int c_mask_card = submatrix[0].length;

    // as before:
    HashMap bcs = new HashMap();

    //  System.out.print(msg+": [P_CR]="+cr_mask.toString()+", [P_CL]="+cl_mask.toString());
    if(g_mask_card == 0 || c_mask_card == 0) {
      System.out.println("(g,c) = (0,0) ?!?"); //System.exit(99);
      return bcs; // das sollte nie auftreten.
    }

    if(   g_mask_card < GENE_SIZE_CONSTRAINT
       || c_mask_card < CHIP_SIZE_CONSTRAINT) return bcs;   // size constraints

    BitSet[] blocks;
    //if(g_mask_card <= c_mask_card)
    blocks = blockTheMatrix_NDS(submatrix, false); //true); // g_mask,c_mask, false); //true);
    //else
    //blocks = blockTheMatrix_Switch_NDS(submatrix, false); //g_mask, c_mask, false); //true);

    BitSet header = blocks[0];
    BitSet b_BC   = blocks[1];
    BitSet cr     = blocks[2];
    BitSet cl     = blocks[3];

    // -------------------------------------------------------------------------
    // all made of zeros (SHOULD NEVER HAPPEN)...
    if(   cr.cardinality() == 0
          && b_BC.cardinality() == 0) {
      System.out.println("case 1");
      // debug
      System.out.println("(cr,b_BC) = (0,0) ?!?");
      //System.exit(1);
      return bcs; //??
    }

    if(   header.cardinality() == g_mask_card
       && cl.cardinality() == c_mask_card) {
      System.out.println("case 2");
     // debug
     System.out.println("all zeros. Can this be ???");
     //System.exit(11);
     return bcs;
   }

   // -------------------------------------------------------------------------
   // (+) no zero eck:
   if(header.cardinality() == 0) {   // weil je to post-condition koja to osigura
       System.out.println("case 3");
     // debug
     if(cl.cardinality() > 0) {
       System.out.println("Impossible: (hdr, cl) = (0, >0) !!!");
       //System.exit(2);
     }

     BitSet g_mask = new BitSet(g_mask_card);
     g_mask.flip(0,g_mask_card);
     BitSet c_mask = new BitSet(c_mask_card);
     c_mask.flip(0, c_mask_card);

     bcs.put(g_mask, c_mask);

     System.out.println("\nEND CASE: "+g_mask.toString()+", "+c_mask.toString());
     /*if(print_flag) {
           System.out.println("\nEND CASE: "+g_mask.toString()+", "+c_mask.toString());
           print_submatrix_content(g_mask, c_mask);
           System.out.println();
         }*/

     // TRANSLATE BACK?????
     return bcs;
   }

   // ==============================
   // mozda nesto bespotrebno stvaram: clean afterwards:
/*
   HashMap newIdx_alpha_g = new HashMap();
   HashMap newIdx_alpha_c = new HashMap();
   for(int i=0; i<g_mask_card; i++) newIdx_alpha_g.put(new Integer(i), new Integer(i));
   int ni = 0;
   for(int i=0; i<c_mask_card; i++)
     if(cr.get(i)) newIdx_alpha_c.put(new Integer(ni++), new Integer(i));

   HashMap newIdx_beta_g = new HashMap();
   HashMap newIdx_beta_c = new HashMap();
   for(int i=0; i<c_mask_card; i++) newIdx_beta_c.put(new Integer(i), new Integer(i));
   ni = 0;
   for(int i=0; i<g_mask_card; i++)
     if(b_BC.get(i)) newIdx_beta_g.put(new Integer(ni++), new Integer(i));

   HashMap newIdx_gamma_g = new HashMap();
   HashMap newIdx_gamma_c = new HashMap();
   ni = 0;
   for(int i=0; i<g_mask_card; i++)
     if(b_BC.get(i)) newIdx_gamma_g.put(new Integer(ni++), new Integer(i));
   ni = 0;
   for(int i=0; i<c_mask_card; i++)
     if(cl.get(i)) newIdx_gamma_c.put(new Integer(ni++), new Integer(i));
*/

   // ------
  /* System.out.println("copy into a:");
   int[][] new_sm_a = copy_into_sm(submatrix,
                                   getUnion(b_BC, header), cr);

   System.out.println("copy into b:");
   int[][] new_sm_b = copy_into_sm(submatrix,
                                   b_BC, getUnion(cr, cl));

   System.out.println("copy into c:");
   int[][] new_sm_c = copy_into_sm(submatrix,
                                   b_BC, cl);
*/
   // -------------------------------------------------------------------------
   // (+) horizontal zero pojas:
    if(   header.cardinality() > 0
       && cl.cardinality() == c_mask_card) {
      System.out.println("case 4");
      // debug
      if(cr.cardinality() < c_mask_card) {
        System.out.println("Impossible: (hdr, cl;cr) = (>0, ==c_mask, !=c_mask) !!!");
        //System.exit(3);
      }

      HashMap newIdx_beta_g = new HashMap();
      HashMap newIdx_beta_c = new HashMap();
      for(int i=0; i<c_mask_card; i++) newIdx_beta_c.put(new Integer(i), new Integer(i));
      int ni = 0;
      for(int i=0; i<g_mask_card; i++)
        if(b_BC.get(i)) newIdx_beta_g.put(new Integer(ni++), new Integer(i));
      int[][] new_sm_b = copy_into_sm(submatrix,b_BC, getUnion(cr, cl));

      bcs = computeBiclusters_NewDataStructure(new_sm_b, cr_mask, cl_mask, msg, (base+1), false);
      return translate_back_bcs(bcs, g_mask_card, c_mask_card,
                                newIdx_beta_g, newIdx_beta_c);
    }

    // -------------------------------------------------------------------------
    // (+) vertical zero pojas:
    if(header.cardinality() == g_mask_card) { // && cr.cardinality() > 0) {

      System.out.println("case 5");
     // debug
      if(cr.cardinality() == 0) {
        System.out.println("Impossible: (hdr, cr) = (g_mask, 0) ???");
        //System.exit(44);
      }
      // debug
      if(b_BC.cardinality() < g_mask_card) {
        System.out.println("Impossible: (hdr, b_BC) = (==g_mask, !=g_mask) !!!");
        //System.exit(4);
      }

      if(   msg.equals("top") == false
         && (isSubset(cr, cr_mask) || isSubset(cr, cl_mask)))
        return bcs;

      HashMap newIdx_alpha_g = new HashMap();
      HashMap newIdx_alpha_c = new HashMap();
      for(int i=0; i<g_mask_card; i++) newIdx_alpha_g.put(new Integer(i), new Integer(i));
      int ni = 0;
      for(int i=0; i<c_mask_card; i++)
        if(cr.get(i)) newIdx_alpha_c.put(new Integer(ni++), new Integer(i));

      int[][] new_sm_a = copy_into_sm(submatrix, getUnion(b_BC, header), cr);

      bcs = computeBiclusters_NewDataStructure(new_sm_a,
                                               getIntersection(cr, cr_mask),
                                               getIntersection(cr, cl_mask),
                                               msg, (base+1), false);
      return translate_back_bcs(bcs, g_mask_card, c_mask_card,newIdx_alpha_g, newIdx_alpha_c);
    }

    // -------------------------------------------------------------------------
    // "normal" zero eck:
    if(header.cardinality() > 0) {

     System.out.println("case 6");
     // debug:
      if(cl.cardinality() == 0) {
        System.out.println("Impossible: (hdr, cl) = (> 0, 0) !!!");
        //System.exit(5);
      }
      if(cr.cardinality() == 0) {
        System.out.println("Impossible: (hdr, cr) = (> 0, 0) !!!");
        //System.exit(55);
      }

      // -----------------------------------------------------------------
      if (msg.equals("top")) {  // solve all!!!!
        System.out.println("case 7");

/*
        HashMap alpha = computeBiclusters(g_mask, cr,
                                          getIntersection(cr, cr_mask),
                                          getIntersection(cr, cl_mask),
                                          msg, (base + 1), false); // ok.?

        HashMap gamma = computeBiclusters(b_BC, cl,
                                          getIntersection(cl, cr_mask),
                                          getIntersection(cl, cl_mask),
                                          msg, (base+1), false);

        HashMap beta = computeBiclusters(b_BC, c_mask,
                                         cr, cl,
                                         "b", (base+1), false);

        return mergeHashes(mergeHashes(alpha, gamma), beta);
 */
        HashMap newIdx_alpha_g = new HashMap();
        HashMap newIdx_alpha_c = new HashMap();
        for(int i=0; i<g_mask_card; i++) newIdx_alpha_g.put(new Integer(i), new Integer(i));
        int ni = 0;
        for(int i=0; i<c_mask_card; i++)
          if(cr.get(i)) newIdx_alpha_c.put(new Integer(ni++), new Integer(i));
        int[][] new_sm_a = copy_into_sm(submatrix, getUnion(b_BC, header), cr);

        HashMap alpha = computeBiclusters_NewDataStructure(new_sm_a,
            getIntersection(cr, cr_mask),
            getIntersection(cr, cl_mask),
            msg, (base+1), false);
        alpha = translate_back_bcs(alpha, g_mask_card, c_mask_card,newIdx_alpha_g, newIdx_alpha_c);
        if(base == 0) {
          System.out.println("alpha top = "+alpha.size());
        }

        HashMap newIdx_gamma_g = new HashMap();
        HashMap newIdx_gamma_c = new HashMap();
        ni = 0;
        for(int i=0; i<g_mask_card; i++)
          if(b_BC.get(i)) newIdx_gamma_g.put(new Integer(ni++), new Integer(i));
        ni = 0;
        for(int i=0; i<c_mask_card; i++)
          if(cl.get(i)) newIdx_gamma_c.put(new Integer(ni++), new Integer(i));

        int[][] new_sm_c = copy_into_sm(submatrix, b_BC, cl);

        HashMap gamma = computeBiclusters_NewDataStructure(new_sm_c,
            getIntersection(cl, cr_mask),
            getIntersection(cl, cl_mask),
            msg, (base+1), false);
        gamma = translate_back_bcs(gamma, g_mask_card, c_mask_card,newIdx_gamma_g, newIdx_gamma_c);
        if(base == 0)
          System.out.println("gamma top = "+gamma.size());

        HashMap newIdx_beta_g = new HashMap();
        HashMap newIdx_beta_c = new HashMap();
        for(int i=0; i<c_mask_card; i++) newIdx_beta_c.put(new Integer(i), new Integer(i));
        ni = 0;
        for(int i=0; i<g_mask_card; i++)
          if(b_BC.get(i)) newIdx_beta_g.put(new Integer(ni++), new Integer(i));
        int[][] new_sm_b = copy_into_sm(submatrix, b_BC, getUnion(cr, cl));

        HashMap beta = computeBiclusters_NewDataStructure(new_sm_b,
            cr, cl,
            "b", (base+1), false);
        beta = translate_back_bcs(bcs, g_mask_card, c_mask_card, newIdx_beta_g, newIdx_beta_c);
        if(base == 0)
          System.out.println("beta top = "+beta.size());

        return mergeHashes(mergeHashes(alpha, gamma), beta);
      }

      // -----------------------------------------------------------------
      else {
       System.out.println("case 8");

        HashMap alpha = new HashMap();
        HashMap beta  = new HashMap();

        // -*-*-*-*-
        if((isSubset(cr, cr_mask) || isSubset(cr, cl_mask))
           &&
           (isSubset(cl, cr_mask) || isSubset(cl, cl_mask))) {
          System.out.println("case 9");

          HashMap newIdx_beta_g = new HashMap();
          HashMap newIdx_beta_c = new HashMap();
          for(int i=0; i<c_mask_card; i++) newIdx_beta_c.put(new Integer(i), new Integer(i));
          int ni = 0;
          for(int i=0; i<g_mask_card; i++)
            if(b_BC.get(i)) newIdx_beta_g.put(new Integer(ni++), new Integer(i));
          int[][] new_sm_b = copy_into_sm(submatrix, b_BC, getUnion(cr, cl));

          beta = computeBiclusters_NewDataStructure(new_sm_b,
              cr_mask, cl_mask,
              "b", (base+1), false);

          beta = translate_back_bcs(beta, g_mask_card, c_mask_card,newIdx_beta_g, newIdx_beta_c);
        }
        // -*-*-*-*-
        else
        if(isSubset(cr, cr_mask) || isSubset(cr, cl_mask)) { // "regular" case
          System.out.println("case 10");

          HashMap newIdx_beta_g = new HashMap();
          HashMap newIdx_beta_c = new HashMap();
          for(int i=0; i<c_mask_card; i++) newIdx_beta_c.put(new Integer(i), new Integer(i));
          int ni = 0;
          for(int i=0; i<g_mask_card; i++)
            if(b_BC.get(i)) newIdx_beta_g.put(new Integer(ni++), new Integer(i));
          int[][] new_sm_b = copy_into_sm(submatrix, b_BC, getUnion(cr, cl));

          beta = computeBiclusters_NewDataStructure(new_sm_b,
              cr_mask, cl_mask,
              "b", (base+1), false);
          beta = translate_back_bcs(beta, g_mask_card, c_mask_card,newIdx_beta_g, newIdx_beta_c);
        }

        // -*-*-*-*-
        else
        if(isSubset(cl, cr_mask) || isSubset(cl, cl_mask)) { // another "regular" case
         System.out.println("case 11");

/*
          alpha = computeBiclusters(g_mask, cr,
                                    getIntersection(cr, cr_mask),
                                    getIntersection(cr, cl_mask),
                                    "a", (base+1), false);

          beta = computeBiclusters(b_BC, c_mask,
                                   cr_mask, cl_mask,
                                   "b",(base+1), false);

          beta = cleanMasked(beta, cr);
 */
          HashMap newIdx_alpha_g = new HashMap();
          HashMap newIdx_alpha_c = new HashMap();
          for(int i=0; i<g_mask_card; i++) newIdx_alpha_g.put(new Integer(i), new Integer(i));
          int ni = 0;
          for(int i=0; i<c_mask_card; i++)
            if(cr.get(i)) newIdx_alpha_c.put(new Integer(ni++), new Integer(i));
          int[][] new_sm_a = copy_into_sm(submatrix, getUnion(b_BC, header), cr);

          alpha = translate_back_bcs(
              computeBiclusters_NewDataStructure(new_sm_a,
                                                 getIntersection(cr, cr_mask),
                                                 getIntersection(cr, cl_mask),
                                                 "a", (base+1), false),
              g_mask_card, c_mask_card,
              newIdx_alpha_g,
              newIdx_alpha_c);

          HashMap newIdx_beta_g = new HashMap();
          HashMap newIdx_beta_c = new HashMap();
          for(int i=0; i<c_mask_card; i++) newIdx_beta_c.put(new Integer(i), new Integer(i));
          ni = 0;
          for(int i=0; i<g_mask_card; i++)
            if(b_BC.get(i)) newIdx_beta_g.put(new Integer(ni++), new Integer(i));
          int[][] new_sm_b = copy_into_sm(submatrix, b_BC, getUnion(cr, cl));

          beta = translate_back_bcs(
              computeBiclusters_NewDataStructure(new_sm_b,
                                                 cr_mask, cl_mask,
                                                 "b", (base+1), false),
              g_mask_card, c_mask_card,
              newIdx_beta_g,
              newIdx_beta_c);

          beta = cleanMasked(beta, cr);
        }

        // -*-*-*-*-
        else {
          System.out.println("case 12");

          HashMap newIdx_alpha_g = new HashMap();
          HashMap newIdx_alpha_c = new HashMap();
          for(int i=0; i<g_mask_card; i++) newIdx_alpha_g.put(new Integer(i), new Integer(i));
          int ni = 0;
          for(int i=0; i<c_mask_card; i++)
            if(cr.get(i)) newIdx_alpha_c.put(new Integer(ni++), new Integer(i));
          int[][] new_sm_a = copy_into_sm(submatrix, getUnion(b_BC, header), cr);

          // the irregular case:
          alpha = translate_back_bcs(
              computeBiclusters_NewDataStructure(new_sm_a,
                                                 getIntersection(cr, cr_mask),
                                                 getIntersection(cr, cl_mask),
                                                 "a", (base+1), false),
              g_mask_card, c_mask_card,
              newIdx_alpha_g,
              newIdx_alpha_c);
          /*
                     alpha = computeBiclusters(g_mask, cr,
                                    getIntersection(cr, cr_mask),
                                    getIntersection(cr, cl_mask),
                                    "a", (base+1), false);
           */
          HashMap newIdx_gamma_g = new HashMap();
          HashMap newIdx_gamma_c = new HashMap();
          ni = 0;
          for(int i=0; i<g_mask_card; i++)
            if(b_BC.get(i)) newIdx_gamma_g.put(new Integer(ni++), new Integer(i));
          ni = 0;
          for(int i=0; i<c_mask_card; i++)
            if(cl.get(i)) newIdx_gamma_c.put(new Integer(ni++), new Integer(i));
          int[][] new_sm_c = copy_into_sm(submatrix, b_BC, cl);

          HashMap gamma = translate_back_bcs(
              computeBiclusters_NewDataStructure(new_sm_c,
                                                 getIntersection(cl, cr_mask),
                                                 getIntersection(cl, cl_mask),
                                                 "a", (base+1), false),
              g_mask_card, c_mask_card,
              newIdx_gamma_g,
              newIdx_gamma_c);

          /*HashMap gamma = computeBiclusters(b_BC, cl,
                                            getIntersection(cl, cr_mask),
                                            getIntersection(cl, cl_mask),
                                            "a", (base+1), false);
           */

          alpha = mergeHashes(alpha, gamma);

          HashMap newIdx_beta_g = new HashMap();
          HashMap newIdx_beta_c = new HashMap();
          for(int i=0; i<c_mask_card; i++) newIdx_beta_c.put(new Integer(i), new Integer(i));
          ni = 0;
          for(int i=0; i<g_mask_card; i++)
            if(b_BC.get(i)) newIdx_beta_g.put(new Integer(ni++), new Integer(i));
          int[][] new_sm_b = copy_into_sm(submatrix, b_BC, getUnion(cr, cl));

          beta = translate_back_bcs(
              computeBiclusters_NewDataStructure(new_sm_b,
                                                 blocks[2], blocks[3],
                                                 "b", (base+1), false),
              g_mask_card, c_mask_card,
              newIdx_beta_g,
              newIdx_beta_c);
          /*
          beta = computeBiclusters(b_BC, c_mask,
             blocks[2], blocks[3],
                                   "b",(base+1), false);
           */
          beta = cleanMasked(beta, cr_mask);
          beta = cleanMasked(beta, cl_mask);
        }

        return mergeHashes(alpha,beta);
      }

      // -----------------------------------------------------------------

    }

    System.out.println("arrived here? SHOULD NEVER HAPPEN!");
    //System.exit(2);
    return bcs;
  }

  // ===========================================================================
  BitSet[] blockTheMatrix_NDS(int[][] sm, boolean py) {
    //  System.out.println();
    /*
      if(py)
        System.out.println("\nBlocking began with:"+
                           " genemask = "+g_mask.toString()+
                           ", chipmask = "+c_mask.toString());
     */
    BitSet[] blocks = new BitSet[4];
      //BitSet h_mask = blocks[0];
      //BitSet b_BC   = blocks[1];
      //BitSet cr_mask = blocks[2];
      //BitSet cl_mask = blocks[3];

      //System.out.println("\nBTM: "+sm.length+", "+sm[0].length);

      // CREATE GENE STARS
      BitSet[] localGeneStars = new BitSet[sm.length];
      for(int i= 0; i<sm.length; i++) {
        localGeneStars[i] = new BitSet(sm[0].length);
        for(int j = 0; j<sm[0].length; j++)
          if(sm[i][j] == 1) localGeneStars[i].set(j);
      }

      // CREATE CHIP STARS
      BitSet[] localChipStars = new BitSet[sm[0].length];
      for(int i= 0; i<sm[0].length; i++) {
        localChipStars[i] = new BitSet(sm.length);
        for(int j = 0; j<sm.length; j++)
          if(sm[j][i] == 1) localChipStars[i].set(j);
      }

      //System.out.println("lgs = "+localGeneStars.length);
      //System.out.println("lcs = "+localChipStars.length);

      // get the header 0-block, if any
      BitSet[] zero_block = getZeroBlockHeuristic_Random_NDS(localGeneStars, localChipStars) ; //g_mask, c_mask);
      //BitSet[] zero_block = getZeroBlockHeuristic_LeastZero(g_mask, c_mask);
      //BitSet[] zero_block = getZeroBlockHeuristic_MostZero(g_mask, c_mask);
      //BitSet[] zero_block = getZeroBlockHeuristic_HalfZero(g_mask, c_mask);
      //zb[0] = hdr, zb[1] = cr

      BitSet cl;
      if(   zero_block[0].cardinality() > 0
            //         && zero_block[0].cardinality() < g_mask.cardinality()
            //         && zero_block[1].cardinality() == c_mask.cardinality()) {
         && zero_block[0].cardinality() < sm.length
         && zero_block[1].cardinality() == sm[0].length) {
         cl = (BitSet)zero_block[1].clone();
      }
      else {
        //BitSet
        cl = new BitSet(sm[0].length) ;//chipCount);
        cl.or(zero_block[1]);
        cl.flip(0,sm[0].length); //chipCount);
        // cl.and(c_mask);       // get the cl
      }

      BitSet b_bc;
      if(//zero_block[0].cardinality() == g_mask.cardinality()
        zero_block[0].cardinality() == sm.length
        && zero_block[1].cardinality() > 0) {
        b_bc = (BitSet)zero_block[0].clone();
      }
      else {
        // BitSet
        b_bc = new BitSet(sm.length); //geneCount);
        b_bc.or(zero_block[0]);
        b_bc.flip(0,sm.length);//geneCount);
        //b_bc.and(g_mask);
      }
      blocks[0] = zero_block[0]; // hdr
      blocks[1] = b_bc;
      blocks[2] = zero_block[1]; // cr
      blocks[3] = cl; // cl

      if(py){
      System.out.println("Blocking returns with: hdr = "+blocks[0].toString() +
                         ", bc = "+blocks[1].toString() +
                         ", cr = "+blocks[2].toString() +
                         ", cl = "+blocks[3].toString());
      System.out.println(); }

      return blocks;
  }

  // ===========================================================================
  BitSet[] blockTheMatrix_Switch_NDS(int[][] sm, boolean py) {
    return null;
  }

  // ===========================================================================
  HashMap translate_back_bcs(HashMap bcs, int gd, int cd,
                             HashMap gh, HashMap ch) {

    // debug purposes:

    /*System.out.print("genes mapping: ");
    Set keys = gh.keySet();
    Object[] arr = keys.toArray();
    for(int a = 0; a <arr.length; a++) {
      System.out.print(arr[a].toString()+" -> "+gh.get(arr[a]).toString()+"; ");
    }
    //Set
    System.out.print("\nchips mapping: ");
    keys = ch.keySet();
    //Object[]
    arr = keys.toArray();
    for(int a = 0; a <arr.length; a++) {
      System.out.print(arr[a].toString()+" -> "+ch.get(arr[a]).toString()+"; ");
    }
    System.out.println();
*/

    HashMap tr_bcs = new HashMap();

  Set
    keys = bcs.keySet();
  Object[]
    arr = keys.toArray();
    for(int i= 0; i<arr.length; i++) {
      BitSet gs = (BitSet)((BitSet)arr[i]).clone();
      BitSet cs = (BitSet)((BitSet)bcs.get(arr[i])).clone();

      BitSet ngs = new BitSet(gd);
      BitSet ncs = new BitSet(cd);

      int k;
      for(k = gs.nextSetBit(0); k >= 0; k = gs.nextSetBit(k+1))
        ngs.set(((Integer)gh.get(new Integer(k))).intValue());
      for(k = cs.nextSetBit(0); k >= 0; k = cs.nextSetBit(k+1))
        ncs.set(((Integer)ch.get(new Integer(k))).intValue());

      // System.out.println("T.ed: "+ngs.toString()+", "+ncs.toString());
      tr_bcs.put(ngs, ncs);
    }
    System.out.println(tr_bcs.size());
    return tr_bcs;
  }

  // ===========================================================================
  private int[][] copy_into_sm(int[][] mtrx, BitSet gs, BitSet cs) {

    //System.out.println("check = "+mtrx.length+","+mtrx[0].length);
    //System.out.println("NEW GS = "+gs.toString());
    //System.out.println("NEW CS = " +cs.toString());

    int[][] sm = new int[gs.cardinality()][cs.cardinality()];
    int i;
    int g = 0;
    for(i = gs.nextSetBit(0); i>=0; i = gs.nextSetBit(i+1)) {
      int j;
      int c = 0;
      for(j = cs.nextSetBit(0); j>=0; j=cs.nextSetBit(j+1)) {
        // System.out.println("g = "+g+", c = "+c);
        sm[g][c] = mtrx[i][j];
        c++;
      }
      g++;
    }

    return sm; //null;
  }

  // ===========================================================================
  BitSet[] blockTheMatrix(BitSet g_mask,BitSet c_mask, boolean print_yes) {

  //  System.out.println();

    if(print_yes)
      System.out.println("\nBlocking began with:"+
                         " genemask = "+g_mask.toString()+
                         ", chipmask = "+c_mask.toString());

    BitSet[] blocks = new BitSet[4];
    //BitSet h_mask = blocks[0];
    //BitSet b_BC   = blocks[1];
    //BitSet cr_mask = blocks[2];
    //BitSet cl_mask = blocks[3];

    // get the header 0-block, if any
    BitSet[] zero_block = getZeroBlockHeuristic_Random(g_mask, c_mask);
    //BitSet[] zero_block = getZeroBlockHeuristic_LeastZero(g_mask, c_mask);
    //BitSet[] zero_block = getZeroBlockHeuristic_MostZero(g_mask, c_mask);
    //BitSet[] zero_block = getZeroBlockHeuristic_HalfZero(g_mask, c_mask);
    //zb[0] = hdr, zb[1] = cr

    BitSet cl;
    if(   zero_block[0].cardinality() > 0
       && zero_block[0].cardinality() < g_mask.cardinality()
       && zero_block[1].cardinality() == c_mask.cardinality()) {
       cl = (BitSet)zero_block[1].clone();
    }
    else {
      //BitSet
      cl = new BitSet(chipCount);
      cl.or(zero_block[1]);
      cl.flip(0,chipCount);
      cl.and(c_mask);       // get the cl
    }

    BitSet b_bc;
    if(zero_block[0].cardinality() == g_mask.cardinality()
       // && zero_block[1].cardinality() < c_mask.cardinality()
       && zero_block[1].cardinality() > 0) {
      b_bc = (BitSet)zero_block[0].clone();
    }
    else {
      // BitSet
      b_bc = new BitSet(geneCount);
      b_bc.or(zero_block[0]);
      b_bc.flip(0,geneCount);
      b_bc.and(g_mask);
    }
    blocks[0] = zero_block[0]; // hdr
    blocks[1] = b_bc;
    blocks[2] = zero_block[1]; // cr
    blocks[3] = cl; // cl

    if(print_yes){
    System.out.println("Blocking returns with: hdr = "+blocks[0].toString() +
                       ", bc = "+blocks[1].toString() +
                       ", cr = "+blocks[2].toString() +
                       ", cl = "+blocks[3].toString());
    System.out.println(); }

    return blocks;
  }

  // ===========================================================================
  BitSet[] blockTheMatrix_Switch(BitSet g_mask,BitSet c_mask, boolean print_yes) {

  //  System.out.print("s\n");

    if(print_yes)
      System.out.println("\nBlocking began with:"+
                         " genemask = "+g_mask.toString()+
                         ", chipmask = "+c_mask.toString());

    BitSet[] blocks = new BitSet[4];
    //BitSet h_mask = blocks[0];
    //BitSet b_BC   = blocks[1];
    //BitSet cr_mask = blocks[2];
    //BitSet cl_mask = blocks[3];

    // get the header 0-block (which has INVERTED 0-eck dimensions!), if any
    BitSet[] zero_block = getZeroBlockHeuristic_Random_Switch(g_mask, c_mask);
    //zb[0] = hdr in chip d, zb[1] = cr in gene d!!!

    // ------------------------------- invert all:

    BitSet cl;
    if(   zero_block[0].cardinality() > 0
          && zero_block[0].cardinality() < c_mask.cardinality()
        //&& zero_block[0].cardinality() < g_mask.cardinality()
          && zero_block[1].cardinality() == g_mask.cardinality()) {
        //&& zero_block[1].cardinality() == c_mask.cardinality()) {
       cl = (BitSet)zero_block[1].clone();
    }
    else {
      //BitSet
      cl = new BitSet(geneCount);
      //cl = new BitSet(chipCount);
      cl.or(zero_block[1]);
      //cl.flip(0,chipCount);
      cl.flip(0,geneCount);
      //cl.and(c_mask);       // get the cl
      cl.and(g_mask);
    }

    // ----------------------------

    BitSet b_bc;
    if(zero_block[0].cardinality() == c_mask.cardinality()
        //zero_block[0].cardinality() == g_mask.cardinality()
       && zero_block[1].cardinality() > 0) {
      b_bc = (BitSet)zero_block[0].clone();
    }
    else {
      // BitSet
      //b_bc = new BitSet(geneCount);
      b_bc = new BitSet(chipCount);
      b_bc.or(zero_block[0]);
      //b_bc.flip(0,geneCount);
      b_bc.flip(0,chipCount);
      //b_bc.and(g_mask);
      b_bc.and(c_mask);
    }

    // ----------------------------------------------------------------
    // SHOULD RETURN EVERYTHING AS THE 'NORMAL' BLOCK-THE-MATRIX:

    blocks[0] = cl; //zero_block[0]; // hdr
    blocks[1] = zero_block[1]; // b_bc;
    blocks[2] = b_bc; //zero_block[1]; // cr
    blocks[3] = zero_block[0]; //cl; // cl

    if(print_yes){
    System.out.println("Blocking returns with: hdr = "+blocks[0].toString() +
                       ", bc = "+blocks[1].toString() +
                       ", cr = "+blocks[2].toString() +
                       ", cl = "+blocks[3].toString());
    System.out.println(); }

    return blocks;
  }

  // ===========================================================================
  BitSet[] getZeroBlockHeuristic_Random_NDS(BitSet[] geneStars, BitSet[] chipStars) { // _mask, BitSet c_mask) {

    //Vector idxs = new Vector();
    //int i = 0;
    //for(i = g_mask.nextSetBit(i); i>=0; i = g_mask.nextSetBit(i+1))
    //idxs.add(new Integer(i));

    Random r = new Random();// pick at random a gene in gm! (for a starting hdr)
    int idx = r.nextInt(geneStars.length);

    // returns the hdr (gene d) & cr (chip d) BitSet's
    //return getHeader(idxs, idx, g_mask, c_mask);
    return getHeader_NDS(idx, geneStars, chipStars);
  }

  // ===========================================================================
  BitSet[] getZeroBlockHeuristic_Random(BitSet g_mask, BitSet c_mask) {

    Vector idxs = new Vector();
    int i = 0;
    for(i = g_mask.nextSetBit(i); i>=0; i = g_mask.nextSetBit(i+1))
      idxs.add(new Integer(i));

    Random r = new Random();// pick at random a gene in gm! (for a starting hdr)
    int idx = r.nextInt(idxs.size());

    // returns the hdr (gene d) & cr (chip d) BitSet's
    return getHeader(idxs, idx, g_mask, c_mask);
  }

  // ===========================================================================
  // does exactly the same as normal getZBR, but switched directions
  BitSet[] getZeroBlockHeuristic_Random_Switch(BitSet g_mask, BitSet c_mask) {

    Vector idxs = new Vector();
    int i = 0;
    for(i = c_mask.nextSetBit(i); i>=0; i = c_mask.nextSetBit(i+1))
      idxs.add(new Integer(i));

    Random r = new Random(); // pick at random a chip in cm! (for a starting hdr)
    int idx = r.nextInt(idxs.size());

    // POST-CONDITION: returns hdr & cr
    // NB: hdr is now in chip d  !!!
    //     cr is now in gene d   !!!
    return getHeader_Switch(idxs, idx, g_mask, c_mask);
  }
/*
  // ===========================================================================
  BitSet[] getZeroBlockHeuristic_LeastZero(BitSet g_mask, BitSet c_mask) {

    Vector idxs = new Vector();
    int i = 0;
    for(i = g_mask.nextSetBit(i); i>=0; i = g_mask.nextSetBit(i+1))
      idxs.add(new Integer(i));

    int min_zeroes_i = 0;
    int min_zeroes = (int)Double.MAX_VALUE;
    for(int j=0; j<idxs.size(); j++) {
      int ii = ((Integer)idxs.get(j)).intValue();
      BitSet p = (BitSet)geneStars[ii].clone();
      p.and(c_mask);
      if(p.cardinality() < min_zeroes) {
        min_zeroes = p.cardinality();
        min_zeroes_i = j;
        if(p.cardinality() == 0) break;
      }
    }

    int idx = min_zeroes_i;

    return getHeader(idxs, idx, g_mask, c_mask);
  }

  // ===========================================================================
  BitSet[] getZeroBlockHeuristic_LeastZeroSwitch(BitSet g_mask, BitSet c_mask) {

    Vector idxs = new Vector();
    int i = 0;
    for(i = c_mask.nextSetBit(i); i>=0; i = c_mask.nextSetBit(i+1))
      idxs.add(new Integer(i));

    int min_zeroes_i = 0;
    int min_zeroes = (int)Double.MAX_VALUE;
    for(int j=0; j<idxs.size(); j++) {
      int ii = ((Integer)idxs.get(j)).intValue();
      BitSet p = (BitSet)chipStars[ii].clone();
      p.and(c_mask);
      if(p.cardinality() < min_zeroes) {
        min_zeroes = p.cardinality();
        min_zeroes_i = j;
        if(p.cardinality() == 0) break;
      }
    }

    int idx = min_zeroes_i;

    return getHeaderSwitch(idxs, idx, g_mask, c_mask);
  }
*/
/*
  // ===========================================================================
  BitSet[] getZeroBlockHeuristic_MostZero(BitSet g_mask, BitSet c_mask) {

    Vector idxs = new Vector();
    int i = 0;
    for(i = g_mask.nextSetBit(i); i>=0; i = g_mask.nextSetBit(i+1))
      idxs.add(new Integer(i));

    int max_zeroes_i = 0;
    int max_zeroes = -(int)Double.MAX_VALUE;
    for(int j=0; j<idxs.size(); j++) {
      int ii = ((Integer)idxs.get(j)).intValue();
      BitSet p = (BitSet)geneStars[ii].clone();
      p.and(c_mask);
      if(p.cardinality() > max_zeroes) {
        max_zeroes = p.cardinality();
        max_zeroes_i = j;
        if(p.cardinality() == g_mask.cardinality()) break;
      }
    }

    int idx = max_zeroes_i;

    return getHeader(idxs, idx, g_mask, c_mask);
  }

  // ===========================================================================
  BitSet[] getZeroBlockHeuristic_MostZeroSwitch(BitSet g_mask, BitSet c_mask) {

    Vector idxs = new Vector();
    int i = 0;
    for(i = c_mask.nextSetBit(i); i>=0; i = c_mask.nextSetBit(i+1))
      idxs.add(new Integer(i));

    int max_zeroes_i = 0;
    int max_zeroes = -(int)Double.MAX_VALUE;
    for(int j=0; j<idxs.size(); j++) {
      int ii = ((Integer)idxs.get(j)).intValue();
      BitSet p = (BitSet)chipStars[ii].clone();
      p.and(c_mask);
      if(p.cardinality() > max_zeroes) {
        max_zeroes = p.cardinality();
        max_zeroes_i = j;
        if(p.cardinality() == c_mask.cardinality()) break;
      }
    }

    int idx = max_zeroes_i;

    return getHeaderSwitch(idxs, idx, g_mask, c_mask);
  }
*/
/*
  // ===========================================================================
  BitSet[] getZeroBlockHeuristic_HalfZero(BitSet g_mask, BitSet c_mask) {

    Vector idxs = new Vector();
    int i = 0;
    for(i = g_mask.nextSetBit(i); i>=0; i = g_mask.nextSetBit(i+1))
      idxs.add(new Integer(i));

    int closest_to_half_zeroes_i = 0;
    int closest_to_half_zeroes = (int)Double.MAX_VALUE;
    for(int j=0; j<idxs.size(); j++) {
      int ii = ((Integer)idxs.get(j)).intValue();
      BitSet p = (BitSet)geneStars[ii].clone();
      p.and(c_mask);
      if(Math.abs((double)(p.cardinality() - g_mask.cardinality()/2)) < closest_to_half_zeroes) {
        closest_to_half_zeroes = (int)Math.abs((double)(p.cardinality() - g_mask.cardinality()/2));
        closest_to_half_zeroes_i = j;
        if(Math.abs((double)(p.cardinality() - g_mask.cardinality()/2)) == 0) break; // do not need to search more
      }
    }

    int idx = closest_to_half_zeroes_i;

    return  getHeader(idxs, idx, g_mask, c_mask);
  }

  // ===========================================================================
  BitSet[] getZeroBlockHeuristic_HalfZeroSwitch(BitSet g_mask, BitSet c_mask) {

    Vector idxs = new Vector();
    int i = 0;
    for(i = c_mask.nextSetBit(i); i>=0; i = c_mask.nextSetBit(i+1))
      idxs.add(new Integer(i));

    int closest_to_half_zeroes_i = 0;
    int closest_to_half_zeroes = (int)Double.MAX_VALUE;
    for(int j=0; j<idxs.size(); j++) {
      int ii = ((Integer)idxs.get(j)).intValue();
      BitSet p = (BitSet)chipStars[ii].clone();
      p.and(c_mask);
      if(Math.abs((double)(p.cardinality() - c_mask.cardinality()/2)) < closest_to_half_zeroes) {
        closest_to_half_zeroes = (int)Math.abs((double)(p.cardinality() - c_mask.cardinality()/2));
        closest_to_half_zeroes_i = j;
        if(Math.abs((double)(p.cardinality() - c_mask.cardinality()/2)) == 0) break; // do not need to search more
      }
    }

    int idx = closest_to_half_zeroes_i;

    return  getHeaderSwitch(idxs, idx, g_mask, c_mask);
  }
*/

  // ===========================================================================
  // return hdr & cr:
  private BitSet[] getHeader_NDS(int v_idx, BitSet[] geneStars, BitSet[] chipStars) {

    BitSet[] header = new BitSet[2];

    BitSet h_mask = new BitSet(geneCount);
    BitSet cr = new BitSet(chipCount);

    //int r_idx = ((Integer)idxs.get(v_idx)).intValue(); // real idx = gene ID
    int new_r_idx = v_idx;

    BitSet gvec = (BitSet)(geneStars[v_idx].clone());
    //gvec.and(c_mask); // its c_mask

    // FINDS a row containing at least one 0-entry!

    int k = 0;

    // -------------------------------------------------------------------------
    // search for a row(gene) having at least one 0-cell:
    if(gvec.cardinality() == chipStars.length) {

      int n_idx = v_idx; // nije vazno sta ovdje stavim, valjda
      while (gvec.cardinality() == chipStars.length) {
        if(k != v_idx) {
          gvec.clear();
          n_idx = k; //( (Integer) idxs.get(k)).intValue();
          gvec = (BitSet) (geneStars[n_idx].clone());
          // gvec.and(c_mask);
          if (gvec.cardinality() < chipStars.length) {
            v_idx = k; // hoce li biti u redu?
            new_r_idx = n_idx;
            break;
          }
        }
        k++;
        if (k == geneStars.length) break;
      }
    }

    // -------------------------------------------------------------------------
    // if I picked a row having all zeros: all other genes of the 0-eck,
    // will have to be all zeroes themselves:
    if(gvec.cardinality() == 0) {

      h_mask.set(new_r_idx, true);

      // GO THROUGH ALL rows, and "pick" up the h_mask: all those being EQUAL!
      for(int p =0; p<geneStars.length; p++) {
        int rp = p; //((Integer)idxs.get(p)).intValue();
        BitSet proba = (BitSet)geneStars[rp].clone();
        //proba.and(c_mask);
        if(proba.cardinality() == 0)
          h_mask.set(rp, true);
      }

      header[0] = h_mask;
      if(h_mask.cardinality() == geneStars.length)
        header[1] = cr;
      else {
        cr.flip(0, chipStars.length);
        header[1] = cr; //c_mask;
      }
      return header;
    }

    // no zero-cell was found!!
    if(gvec.cardinality() == chipStars.length) {
      header[0] = h_mask;           // is empty; all is a big 1-block!
      // cr.or(c_mask);
      cr.flip(0, chipStars.length);
      header[1] = cr;               // \eq die ganze c_mask!
      return header;
    }

    // -------------------------------------------------------------------------
    // OTHERWISE, a "normal" case... (a 0-eck was found):

    h_mask.set(new_r_idx, true);
    gvec = (BitSet)(geneStars[new_r_idx].clone());
    // gvec.and(c_mask); // its c_mask
    cr = (BitSet)gvec.clone();

    //System.out.println("CHECK PT: cr = "+cr.toString()+", H_MASK = "+new_r_idx+", h_mask = "+h_mask.toString());

    for(int i = 0; i< geneStars.length; i++) {
      int iidx = i; //((Integer)idxs.get(i)).intValue();
      BitSet cand = (BitSet) geneStars[iidx].clone();
      // cand.and(c_mask);
      if(isSubset(cand, cr))
        h_mask.set(iidx, true);
    }

    header[0] = h_mask;
    header[1] = cr;

    // System.out.println("Header returns with: "+header[0].toString()+", "+header[1].toString()+", "+header[2].toString());
    // HDR + CR returned:

    // NB: hdr in the gene dimension
    //     cr in the chip dimension
    return header;
  }

// ===========================================================================
// return hdr & cr:
private BitSet[] getHeader(Vector idxs, int v_idx, BitSet g_mask, BitSet c_mask) {

  BitSet[] header = new BitSet[2];

  BitSet h_mask = new BitSet(geneCount);
  BitSet cr = new BitSet(chipCount);

  int r_idx = ((Integer)idxs.get(v_idx)).intValue(); // real idx = gene ID
  int new_r_idx = r_idx;

  BitSet gvec = (BitSet)(geneStars[r_idx].clone());
  gvec.and(c_mask); // its c_mask

  // FINDS a row containing at least one 0-entry!

  int k = 0;

  // -------------------------------------------------------------------------
  // search for a row(gene) having at least one 0-cell:
  if(gvec.cardinality() == c_mask.cardinality()) {

    int n_idx = r_idx; // nije vazno sta ovdje stavim, valjda
    while (gvec.cardinality() == c_mask.cardinality()) {
      if(k != v_idx) {
        gvec.clear();
        n_idx = ( (Integer) idxs.get(k)).intValue();
        gvec = (BitSet) (geneStars[n_idx].clone());
        gvec.and(c_mask);
        if (gvec.cardinality() < c_mask.cardinality()) {
          v_idx = k; // hoce li biti u redu?
          new_r_idx = n_idx;
          break;
        }
      }
      k++;
      if (k == idxs.size()) break;
    }
  }

  // -------------------------------------------------------------------------
  // if I picked a row having all zeros: all other genes of the 0-eck,
  // will have to be all zeroes themselves:
  if(gvec.cardinality() == 0) {

    h_mask.set(new_r_idx, true);

    // GO THROUGH ALL rows, and "pick" up the h_mask: all those being EQUAL!
    for(int p =0; p<idxs.size(); p++) {
      int rp = ((Integer)idxs.get(p)).intValue();
      BitSet proba = (BitSet)geneStars[rp].clone();
      proba.and(c_mask);
      if(proba.cardinality() == 0)
        h_mask.set(rp, true);
    }

    header[0] = h_mask;
    if(h_mask.cardinality() == g_mask.cardinality())
      header[1] = cr;
    else
      header[1] = c_mask;
    return header;
  }

  // no zero-cell was found!!
  if(gvec.cardinality() == c_mask.cardinality()) {
    header[0] = h_mask;           // is empty; all is a big 1-block!
    cr.or(c_mask);
    header[1] = cr;               // \eq die ganze c_mask!
    return header;
  }

  // -------------------------------------------------------------------------
  // OTHERWISE, a "normal" case... (a 0-eck was found):

  h_mask.set(new_r_idx, true);
  gvec = (BitSet)(geneStars[new_r_idx].clone());
  gvec.and(c_mask); // its c_mask
  cr = (BitSet)gvec.clone();

  //System.out.println("CHECK PT: cr = "+cr.toString()+", H_MASK = "+new_r_idx+", h_mask = "+h_mask.toString());

  for(int i = 0; i< idxs.size(); i++) {
    int iidx = ((Integer)idxs.get(i)).intValue();
    BitSet cand = (BitSet) geneStars[iidx].clone();
    cand.and(c_mask);
    if(isSubset(cand, cr))
      h_mask.set(iidx, true);
  }

  header[0] = h_mask;
  header[1] = cr;

  // System.out.println("Header returns with: "+header[0].toString()+", "+header[1].toString()+", "+header[2].toString());
  // HDR + CR returned:

  // NB: hdr in the gene dimension
  //     cr in the chip dimension
  return header;
}

  // ===========================================================================
  // return hdr & cr (in chip, and gene dim - REVERSED Ds!!!):
  private BitSet[] getHeader_Switch(Vector idxs, int v_idx, BitSet g_mask, BitSet c_mask) {

    BitSet[] header = new BitSet[2];

    //BitSet h_mask = new BitSet(geneCount);
    //BitSet cr = new BitSet(chipCount);
    BitSet h_mask = new BitSet(chipCount);
    BitSet cr = new BitSet(geneCount);

    int r_idx = ((Integer)idxs.get(v_idx)).intValue(); // real idx = gene ID
    int new_r_idx = r_idx;

    //BitSet gvec = (BitSet)(geneStars[r_idx].clone());
    //gvec.and(c_mask); // its c_mask
    BitSet gvec = (BitSet)(chipStars[r_idx].clone());
    gvec.and(g_mask); // its g_mask

    // FINDS a row containing at least one 0-entry!

    int k = 0;

    // -------------------------------------------------------------------------
    // search for a row(gene) having at least one 0-cell:
    /*if(gvec.cardinality() == c_mask.cardinality()) {

      int n_idx = r_idx; // nije vazno sta ovdje stavim, valjda
      while (gvec.cardinality() == c_mask.cardinality()) {
        if(k != v_idx) {
          gvec.clear();
          n_idx = ( (Integer) idxs.get(k)).intValue();
          gvec = (BitSet) (geneStars[n_idx].clone());
          gvec.and(c_mask);
          if (gvec.cardinality() < c_mask.cardinality()) {
            v_idx = k; // hoce li biti u redu?
            new_r_idx = n_idx;
            break;
          }
        }
        k++;
        if (k == idxs.size()) break;
      }
    }*/
    if(gvec.cardinality() == g_mask.cardinality()) {

      int n_idx = r_idx; // nije vazno sta ovdje stavim, valjda
      while (gvec.cardinality() == g_mask.cardinality()) {
        if(k != v_idx) {
          gvec.clear();
          n_idx = ( (Integer) idxs.get(k)).intValue();
          gvec = (BitSet) (chipStars[n_idx].clone());
          gvec.and(g_mask);
          if (gvec.cardinality() < g_mask.cardinality()) {
            v_idx = k; // hoce li biti u redu?
            new_r_idx = n_idx;
            break;
          }
        }
        k++;
        if (k == idxs.size()) break;
      }
    }

    // -------------------------------------------------------------------------
    // if I picked a row having all zeros: all other genes of the 0-eck,
    // will have to be all zeroes themselves:
    /* if(gvec.cardinality() == 0) {

      h_mask.set(new_r_idx, true);

      // GO THROUGH ALL rows, and "pick" up the h_mask: all those being EQUAL!
      for(int p =0; p<idxs.size(); p++) {
        int rp = ((Integer)idxs.get(p)).intValue();
        BitSet proba = (BitSet)geneStars[rp].clone();
        proba.and(c_mask);
        if(proba.cardinality() == 0)
          h_mask.set(rp, true);
      }

      header[0] = h_mask;
      if(h_mask.cardinality() == g_mask.cardinality())
        header[1] = cr;
      else
        header[1] = c_mask;
      return header;
    } */
    if(gvec.cardinality() == 0) {

      h_mask.set(new_r_idx, true);

      // GO THROUGH ALL rows, and "pick" up the h_mask: all those being EQUAL!
      for(int p =0; p<idxs.size(); p++) {
        int rp = ((Integer)idxs.get(p)).intValue();
        BitSet proba = (BitSet)chipStars[rp].clone();
        proba.and(g_mask);
        if(proba.cardinality() == 0)
          h_mask.set(rp, true);
      }

      header[0] = h_mask;
      if(h_mask.cardinality() == c_mask.cardinality())
        header[1] = cr;
      else
        header[1] = g_mask;
      return header;
    }

    // no zero-cell was found!!
    /*if(gvec.cardinality() == c_mask.cardinality()) {
      header[0] = h_mask;           // is empty; all is a big 1-block!
      cr.or(c_mask);
      header[1] = cr;               // \eq die ganze c_mask!
      return header;
    }*/
    if(gvec.cardinality() == g_mask.cardinality()) {
      header[0] = h_mask;           // is empty; all is a big 1-block!
      cr.or(g_mask);
      header[1] = cr;               // \eq die ganze c_mask!
      return header;
    }

    // -------------------------------------------------------------------------
    // OTHERWISE, a "normal" case... (a 0-eck was found):

/*    h_mask.set(new_r_idx, true);
    gvec = (BitSet)(geneStars[new_r_idx].clone());
    gvec.and(c_mask); // its c_mask
    cr = (BitSet)gvec.clone(); */
    h_mask.set(new_r_idx, true);
    gvec = (BitSet)(chipStars[new_r_idx].clone());
    gvec.and(g_mask); // its c_mask
    cr = (BitSet)gvec.clone();

    //System.out.println("CHECK PT: cr = "+cr.toString()+", H_MASK = "+new_r_idx+", h_mask = "+h_mask.toString());

    /*for(int i = 0; i< idxs.size(); i++) {
      int iidx = ((Integer)idxs.get(i)).intValue();
      BitSet cand = (BitSet) geneStars[iidx].clone();
      cand.and(c_mask);
      if(isSubset(cand, cr))
        h_mask.set(iidx, true);
    }*/
    for(int i = 0; i< idxs.size(); i++) {
      int iidx = ((Integer)idxs.get(i)).intValue();
      BitSet cand = (BitSet) chipStars[iidx].clone();
      cand.and(g_mask);
      if(isSubset(cand, cr))
        h_mask.set(iidx, true);
    }

    header[0] = h_mask;
    header[1] = cr;

    // System.out.println("Header returns with: "+header[0].toString()+", "+header[1].toString()+", "+header[2].toString());
    // HDR + CR returned: (in switch are reversed!!!!)
    // NB: hdr in the CHIP dimension
    //     cr in the GENE dimension
    return header;
  }

  // ===========================================================================
  public HashMap reverseHash(HashMap hm) {
    HashMap result = new HashMap();

    //System.out.println("reverse hash HM = "+hm.size());

    Set keys = hm.keySet();
    Object[] karr = keys.toArray();
    for(int i=0; i<karr.length; i++) {
      BitSet g = (BitSet)((BitSet)karr[i]).clone();
      result.put((BitSet)hm.get(g),g);
      g = null;
    }

    //System.out.println("reverse hash result = "+result.size());

    // parameter nulling?!?
    hm = null;
    keys = null;
    karr = null;

    return result;
  }


  // ===========================================================================
  HashMap mergeHashes(HashMap bcs, HashMap hm) {
    bcs.putAll(hm);
    //??
    hm = null;
    return bcs;
  }
  // ===========================================================================
  void printBicluster(BitSet g, BitSet c) {
    System.out.println("new BC:");
    for(int i = 0; i <geneCount; i++)
      if (g.get(i)) System.out.print((i+1)+" ");
    System.out.println();
    for(int i = 0; i <chipCount; i++)
      if (c.get(i)) System.out.print((i+1)+" ");
    System.out.println();
  }

 // ============================================================================
 HashMap cleanMasked(HashMap list, BitSet mask) {
   Set keys = list.keySet();
   Object[] keys_bs = keys.toArray();
   for (int i = 0; i < keys_bs.length; i++) {
     BitSet key_vec = (BitSet) keys_bs[i];
     BitSet map_vec = (BitSet) list.get(key_vec);

     if (map_vec.cardinality() > 0 && isSubset(map_vec, mask))
       list.remove(key_vec);
   }
   return list;
 }

  // ===========================================================================
  private void print_submatrix_content(BitSet g, BitSet c) {
    int i=0;

    System.out.println("*** ");
    for(i = g.nextSetBit(i); i>=0; i=g.nextSetBit(i+1)) {

      BitSet gene = (BitSet)geneStars[i].clone();

      int j = 0;
      for(j = c.nextSetBit(j); j>=0; j=c.nextSetBit(j+1)) {
        if(gene.get(j)) System.out.print("1 ");
          else System.out.print("0 ");
      }

      System.out.println();
    }
  }
  // ===========================================================================
  private BitSet getUnion(Vector bss) {
    BitSet new_bs = new BitSet(chipCount);
    for(int i=0; i< bss.size(); i++) {
      new_bs.or((BitSet)bss.get(i));
    }
    return new_bs;
  }

  // ===========================================================================
  private BitSet getUnion(BitSet bs1, BitSet bs2) {
    BitSet res = (BitSet)bs1.clone();
    res.or(bs2);
    return res;
  }

// ===========================================================================
  private BitSet getUnion(Vector bss, BitSet bs) {
    BitSet new_bs = getUnion(bss);
    new_bs.or(bs);
    return new_bs;
  }

  // ===========================================================================
  private BitSet getIntersection(BitSet bs1, BitSet bs2) {
    BitSet new_bs = (BitSet)bs1.clone();
    new_bs.and(bs2);
    return new_bs;
  }

  // ===========================================================================
  private BitSet getDifference(BitSet bs1, BitSet bs2) {
    BitSet new_bs = (BitSet)bs1.clone();
    new_bs.and(bs2);
    new_bs.flip(0,chipCount);
    new_bs.and(bs1);
    return new_bs;
  }

  // ===========================================================================
  private boolean isSubset(BitSet b_small, BitSet b_large) {
    if(b_small.cardinality() > b_large.cardinality()) return false;
    int i =0;
    for(i = b_small.nextSetBit(i); i>=0; i=b_small.nextSetBit(i+1))
      if(b_large.get(i) == false) return false;
    return true;
  }

  // ===========================================================================
  private boolean intersects(BitSet b_small, BitSet b_large) {
    int i =0;
    for(i = b_small.nextSetBit(i); i>=0; i=b_small.nextSetBit(i+1))
      if(b_large.get(i)) return true;
    return false;
  }

  // ===========================================================================
  int hammingDistance(BitSet o1, BitSet o2) {
    // returns the Hamming Distance, the smaller, they belong "together"
    BitSet res = (BitSet)o1.clone();
    o1.xor(o2);
    return o1.cardinality();
  }

  // ===========================================================================

}
/*
class MyContainer implements Comparable {
  BitSet bitset;
  int idx;

  public MyContainer(BitSet bs, int i) {
    bitset = bs;
    idx = i;
  }

  public int compareTo(Object o) {
    MyContainer other = (MyContainer)o;
    if(bitset.cardinality() < other.bitset.cardinality()) return -1;
    else if(bitset.cardinality() > other.bitset.cardinality())  return 1;
    else return 0;
  }

}
*/
