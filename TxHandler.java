import java.util.ArrayList;
import java.util.Arrays;

public class TxHandler {

    private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.utxoPool = new UTXOPool(utxoPool);

    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        boolean txValid = true;
        double inSum = 0.0;
        double outSum = 0.0;
        int duplicatedIn = 0;
      
        ArrayList<UTXO> allUtxos = this.utxoPool.getAllUTXO();
        
        ArrayList<Transaction.Input> txIn = tx.getInputs();
        for (int i = 0; i < txIn.size(); i++) { //Transaction.Input in: txIn){
        	Transaction.Input in = txIn.get(i);
          //Verificando primeira condi√ß√£o (1)
          //Primeiro verifico se todos os inputs desbloqueiam algum output
          //dentro do UTXOPool.
          UTXO utxo = new UTXO(in.prevTxHash,in.outputIndex);
          if (!this.utxoPool.contains(utxo)){
            return(txValid = false);
          } 
          
          //Verificando segunda condi√ß√£o (2)
          Transaction.Output out = this.utxoPool.getTxOutput(utxo);
          if (!Crypto.verifySignature(out.address,tx.getRawDataToSign(i),in.signature)){
             return (txValid = false);
          }
          
          //Verificando a terceira condiÁ„o.
          //N„o posso ter mais do que um input numa transaÁ„o apontando para o mesmo UTXO
          for (UTXO ut: allUtxos) {
        	  if (Arrays.equals(in.prevTxHash, ut.getTxHash()) && (in.outputIndex == ut.getIndex())) {
        		  duplicatedIn++;
        	  }
          }
          if (duplicatedIn > 1) {
          	return (txValid = false);
          }
          
          //Somando os valores de input para verificar condi√ß√£o 5.
          inSum += out.value;
        }

        
        
        //Verificando quarta condi√ß√£o (4)
        ArrayList<Transaction.Output> txOut = tx.getOutputs();
        for (Transaction.Output out: txOut){
          if (out.value < 0){
            return (txValid = false);
          }
          outSum += out.value;
        }

        //verificando a quinta condi√ß√£o (5)
        if (inSum < outSum){
          return (txValid = false);
        }

        return txValid;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	//UTXOPool temp = new UTXOPool();
    	
    	 	
        // IMPLEMENT THIS
    	Transaction[] validTxs = new Transaction[] {};
    	int doubleSpending = 0;
    	
    	//iterando no array de transaÁıes para verificar a validade de cada uma delas.
    	for (int i = 0; i < possibleTxs.length; i++) {
    		Transaction tx = possibleTxs[i];
    		
    		//Primeiro verifico se a transaÁ„o È v·lida individualmente.
    		//SÛ continuo se a transaÁ„o for v·lida.
    		//(pensando num mundo ideal primeiro)
    		if (isValidTx(tx)) {
    			
    			ArrayList<Transaction.Input> inTxs = tx.getInputs();
    			ArrayList<Transaction.Output> outTxs = tx.getOutputs();
    			
    			//Destruo os UTXOs relativos aos inputs da transaÁ„o v·lida.
    			//Para cada input, removo os UTXOs correspondentes.
    			for (Transaction.Input in: inTxs) {
    				utxoPool.removeUTXO(new UTXO(in.prevTxHash,in.outputIndex));
    			}
    			
    			//Adiciono UTXOs novos
    			for (int j = 0; j < outTxs.size(); j++) {
    				Transaction.Output txOut = outTxs.get(j);
    				utxoPool.addUTXO(new UTXO(tx.getHash(),j), txOut);
    			}
    			
    			//Adicionando a transaÁ„o no pool de transaÁıes v·lidas
    			//validTxs = new Transaction(tx);
    		}else {
    			System.out.println("TransaÁ„o inv·lida!");
    		}
    	}
    	
    	return validTxs;
    }

}
