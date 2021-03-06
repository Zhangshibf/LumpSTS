package cat.lump.sts2017.ml;


import java.io.File;
import cat.lump.aq.basics.log.LumpLogger;
import hex.genmodel.utils.DistributionFamily;
import hex.tree.gbm.GBM;
import hex.tree.gbm.GBMModel;
import water.Key;
import water.fvec.Frame;
import water.fvec.NFSFileVec;
import water.parser.ParseDataset;
import water.util.MathUtils;


/**
 * Class to learn and apply a Gradient Boosted Machines on the STS data using the H2O library
 * 
 * @author cristina
 * @since Dec 30, 2016
 */
public class GradientBoostedTreesH2O {
	
	/** Logger */
	private static LumpLogger logger = 
			new LumpLogger (GradientBoostedTreesH2O.class.getSimpleName());

	
	
	/**
	 * Main function to run the class, serves as example
	 * 
	 * @param args
	 * 		-f Input file with the training data features and label as first column
	 *      -t Input file with the test data
	 * 		-m Previously trained model
	 */
	public static void main(String[] args) {
		
		CliGBM cli = new CliGBM();
		cli.parseArguments(args);
		
		String training = null;
		//File scores = null;
		String test = null;
		String model = null;

		training = cli.getTraining();
		//scores = cliGBM.getScores();
		test = cli.getTest();
		model = cli.getModel();
		System.out.println(test);
		NFSFileVec nfs = null;
		File file =  new File(test);
				
		nfs = NFSFileVec.make(file);
	    System.out.println("fet");
	    
	    
		Frame featFrame = ParseDataset.parse(Key.make(), nfs._key);

		//featFrame = Utils.parse_test_file(test);
		System.out.println(featFrame);
		//if (test==null && model==null && training!=null ){
		//	featFrame = Utils.parse_test_file(training);
		//}
		GBMModel.GBMParameters params = new GBMModel.GBMParameters();

	    params._train = featFrame._key;
	    params._distribution =  DistributionFamily.gaussian;
	    params._response_column = featFrame._names[1]; // Row in col 0, dependent in col 1, predictor in col 2
	    params._ntrees = 1;
	    params._max_depth = 1;
	    params._min_rows = 1;
	    params._nbins = 20;
	    // Drop ColV2 0 (row), keep 1 (response), keep col 2 (only predictor), drop remaining cols
	    String[] xcols = params._ignored_columns = new String[featFrame.numCols()-2];
	    xcols[0] = featFrame._names[0];
	    System.arraycopy(featFrame._names,3,xcols,1,featFrame.numCols()-3);
	    params._learn_rate = 1.0f;
	    params._score_each_iteration=true;
	    
	    GBM job = new GBM(params);
	    GBMModel gbm = job.trainModel().get();

	    // Done building model; produce a score column with predictions
	    Frame fr2 = gbm.score(featFrame);
	    //job.response() can be used in place of fr.vecs()[1] but it has been rebalanced
	    double sq_err = new MathUtils.SquareError().doAll(featFrame.vecs()[1],fr2.vecs()[0])._sum;
	    double mse = sq_err/fr2.numRows();
	    System.out.println("79152.12337641386");
	    System.out.println(mse);
	    
	    
	    // Setting the parameters
		/* h2o.gbm(x, y, training_frame, model_id, checkpoint, ignore_const_cols = TRUE, 
			distribution = c("AUTO", "gaussian", "bernoulli", "multinomial", "poisson", "gamma", "tweedie", "laplace", "quantile", "huber"), 
		 	quantile_alpha = 0.5, tweedie_power = 1.5, huber_alpha = 0.9, ntrees = 50, max_depth = 5, min_rows = 10, 
		 	learn_rate = 0.1, learn_rate_annealing = 1, sample_rate = 1, sample_rate_per_class, col_sample_rate = 1, 
		 	col_sample_rate_change_per_level = 1, col_sample_rate_per_tree = 1, nbins = 20, nbins_top_level = 1024, 
		 	nbins_cats = 1024, validation_frame = NULL, balance_classes = FALSE, class_sampling_factors, 
			max_after_balance_size = 5, seed, build_tree_one_node = FALSE, nfolds = 0, fold_column = NULL, 
			fold_assignment = c("AUTO", "Random", "Modulo", "Stratified"), keep_cross_validation_predictions = FALSE, 
			keep_cross_validation_fold_assignment = FALSE, score_each_iteration = FALSE, score_tree_interval = 0, 
			stopping_rounds = 0, 
			stopping_metric = c("AUTO", "deviance", "logloss", "MSE", "AUC", "misclassification", "mean_per_class_error"), 
			stopping_tolerance = 0.001, max_runtime_secs = 0, offset_column = NULL, weights_column = NULL, 
			min_split_improvement = 1e-05, 
			histogram_type = c("AUTO", "UniformAdaptive", "Random", "QuantilesGlobal", "RoundRobin"), 
			max_abs_leafnode_pred, pred_noise_bandwidth = 0, 
			categorical_encoding = c("AUTO", "Enum", "OneHotInternal", "OneHotExplicit", "Binary", "Eigen"))
		*/
		//GBMModel.GBMParameters params = new GBMModel.GBMParameters();
	    //params._ntrees = 50;
	    //params._nfolds = 10;
	    
	    // Advice
	    //If you run cross validation with 1000 trees it will slow down the computation significantly, however, 
	    // running cross validation for hyperparameter search on a smaller dataset using this algorithm yields similar 
	    //hyperparameters as if you were running it on the full dataset.
	   
		
	    //GBMModel gbm = new GBM(params).trainModel().get();
	      // Done building model; produce a score column with predictions
	      //fr2 = gbm.score(fr);

	}


	
}
