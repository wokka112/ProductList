ProductList is an Android application designed to help retail workers track and find product prices.

This application is mainly useful in shops where the electronic point of sale system is not fully up-to-date
and price tags are not on every product. In such shops it is not uncommon to have a customer wanting to buy
something that you do not know the price for. This application aims to solve that by providing a simple 
system where you can record product details - name, barcode, price, category, notes - and then later search
for them via barcode scanning or manually entering details into the phone. Thus, when the customer wishes
to buy an item you have a barcode for but no price for, as long as it has previously been entered into the
app you can scan the barcode with your phone and find the relevant price for said product.

A One Activity Many Fragments approach is being used with an MVVM design pattern and data binding.
LiveData and Room are being used to access data stored in a database, and the Navigation components
are used for transition between fragments.
